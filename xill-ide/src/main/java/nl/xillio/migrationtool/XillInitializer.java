/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.migrationtool;

import me.biesaart.utils.Log;
import nl.xillio.events.Event;
import nl.xillio.events.EventHost;
import nl.xillio.plugins.PluginLoadFailure;
import nl.xillio.plugins.XillPlugin;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.docgen.DocGen;
import nl.xillio.xill.docgen.DocumentationGenerator;
import nl.xillio.xill.docgen.DocumentationParser;
import nl.xillio.xill.docgen.DocumentationSearcher;
import nl.xillio.xill.docgen.data.Parameter;
import nl.xillio.xill.docgen.exceptions.ParsingException;
import nl.xillio.xill.docgen.impl.ConstructDocumentationEntity;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * This {@link Thread} is responsible for loading the plugins an and initializing the language.
 *
 * @author Thomas Biesaart
 */
public class XillInitializer extends Thread {
    private static final String HELP_DIR_PREFIX = "xill_helpfiles";

    private final DocGen docGen;
    private DocumentationParser parser;
    private static final Logger LOGGER = Log.get();
    private static final Path PLUGIN_FOLDER = Paths.get("plugins");
    private XillEnvironment xill;
    private final EventHost<InitializationResult> onLoadComplete = new EventHost<>();
    private final String jarFile;
    private DocumentationSearcher searcher;
    private boolean pluginsLoaded = false;

    private File helpDirectory;

    /**
     * Create a new XillInitializer.
     *
     * @param docGen the DocGen set to use
     */
    public XillInitializer(DocGen docGen) {
        this.docGen = docGen;
        jarFile = getClass().getResource("/editor.html").toExternalForm().replaceAll("editor\\.html", "");

        // Generate a temporary directory to store generated help files
        try {
            helpDirectory = Files.createTempDirectory(HELP_DIR_PREFIX).toFile();
        } catch (IOException e) {
            LOGGER.error("Could not create directory for help files", e);
        }
    }

    @Override
    public void run() {
        parser = docGen.getParser();

        LOGGER.info("Loading Xill language plugins...");

        LOGGER.debug("Initializing loader...");
        initializeLoader();

        if (helpDirectory != null) {
            LOGGER.debug("Generating documentation...");
            generateDocumentation();

            LOGGER.debug("Generating documentation index...");
            generateIndex();

            // Delete the help files on exit, help files are regenerated on each start
            // This is done after generating the help files because else the generated files are not scheduled for deletion
            try {
                FileUtils.forceDeleteOnExit(helpDirectory);
            } catch (IOException e) {
                LOGGER.error("Could not register directory for help files for deletion", e);
            }
        }

        LOGGER.info("Done loading plugins...");

        onLoadComplete.invoke(new InitializationResult(getSearcher()));
    }

    private void generateIndex() {
        try {
            docGen.generateIndex();
        } catch (ParsingException e) {
            LOGGER.error("Failed to generate index", e);
        }
    }

    DocumentationSearcher getSearcher() {
        if (this.searcher == null) {
            this.searcher = docGen.getSearcher();
        }

        return this.searcher;
    }

    private void generateDocumentation() {
        getPlugins().forEach(this::generateDocumentation);
    }

    private void generateDocumentation(XillPlugin plugin) {
        try (DocumentationGenerator generator = generator(plugin.getName())) {
            plugin.getConstructs().forEach(construct -> generateDocumentation(construct, generator));
        } catch (Exception e) {
            throw new XillioRuntimeException("Failed to generate documentation for " + plugin.getClass().getName(), e);
        }
    }

    DocumentationGenerator generator(String name) {
        DocumentationGenerator generator = docGen.getGenerator(name, helpDirectory);
        generator.setProperty("jarFile", jarFile);

        return generator;
    }

    private void generateDocumentation(Construct construct, DocumentationGenerator generator) {
        // When the construct explicitly requests no documentation it will be skipped
        if (construct.hideDocumentation()) {
            return;
        }

        URL url = construct.getDocumentationResource();

        if (url == null) {
            LOGGER.warn("No documentation found for " + construct.getClass().getName());
            return;
        }

        try {
            ConstructDocumentationEntity entity = (ConstructDocumentationEntity) parser.parse(url, construct.getName());
            getSearcher().index(generator.getIdentity(), entity);
            generateParameters(construct, entity);
            entity.setDeprecated(construct.isDeprecated());
            generator.generate(entity);
        } catch (ParsingException e) {
            LOGGER.error("Failed to generate documentation from " + url, e);
        }
    }

    /**
     * Peek into the construct to get it's signature
     *
     * @param construct the construct
     * @param entity    the entity to push the signature into
     */
    private void generateParameters(Construct construct, ConstructDocumentationEntity entity) {
        ConstructContext context = new ConstructContext(Paths.get("."), RobotID.dummyRobot(), RobotID.dummyRobot(), construct, null, null, null, null, null);
        try (ConstructProcessor proc = construct.prepareProcess(context)) {
            List<Parameter> parameters = new ArrayList<>();

            for (int i = 0; i < proc.getNumberOfArguments(); i++) {
                String name = proc.getArgumentName(i);
                String type = proc.getArgumentType(i);
                String defaultValue = proc.getArgumentDefault(i);

                Parameter parameter = new Parameter(type, name);
                if (defaultValue != null) {
                    parameter.setDefault(defaultValue);
                }

                parameters.add(parameter);
            }

            entity.setParameters(parameters);
        }
    }

    private void initializeLoader() {
        xill = Loader.getXill();

        try {
            xill.addFolder(PLUGIN_FOLDER);
            xill.loadPlugins();
        } catch (IOException e) {
            LOGGER.error("Failed to create plugin dir", e);
        }
        pluginsLoaded = true;
    }

    /**
     * List the plugins.
     *
     * @return a list of all loaded plugins
     */
    public List<XillPlugin> getPlugins() {
        // Wait for all plugins to be loaded.
        while (!pluginsLoaded) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.error("Failed to list the plugins.", e);
            }
        }

        return new ArrayList<>(xill.getPlugins());
    }

    /**
     * List the plugins with missing licenses.
     *
     * @return a list of erroneous plugins
     */
    public List<PluginLoadFailure> getMissingLicensePlugins() {
        // Wait for all plugins to be loaded.
        while (!pluginsLoaded) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.error("Failed to list the plugins.", e);
            }
        }

        return new ArrayList<>(xill.getMissingLicensePlugins());
    }

    /**
     * This event is called every time the initializer is done loading plugins.
     *
     * @return the event
     */
    public Event<InitializationResult> getOnLoadComplete() {
        return onLoadComplete.getEvent();
    }

    /**
     * @return The directory help files are stored in
     */
    public File getHelpDirectory() {
        return helpDirectory;
    }

    public static class InitializationResult {
        private final DocumentationSearcher searcher;

        public InitializationResult(DocumentationSearcher searcher) {
            this.searcher = searcher;
        }

        public DocumentationSearcher getSearcher() {
            return searcher;
        }
    }
}
