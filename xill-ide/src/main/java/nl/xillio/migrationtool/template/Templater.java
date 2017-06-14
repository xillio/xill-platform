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
package nl.xillio.migrationtool.template;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import me.biesaart.utils.Log;
import nl.xillio.util.XillioHomeFolder;
import nl.xillio.xill.api.XillEnvironment;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for creating robots from templates.
 */
public class Templater {

    /**
     * Default path to template files.
     */
    public static final Path DEFAULT_TEMPLATES_PATH = Paths.get(XillioHomeFolder.forXillIDE().toURI()).resolve("templates");
    private static final Configuration CONFIG = new Configuration(Configuration.VERSION_2_3_22);
    private Path templatesDirectory;
    private static final Logger LOGGER = Log.get();

    /**
     * Default constructor. Sets up the {@link Configuration} and default templates.
     *
     * @throws IOException if a problem occurs during creating default folders/templates
     */
    public Templater() throws IOException {
        templatesDirectory = Templater.DEFAULT_TEMPLATES_PATH;
        if (!Files.exists(templatesDirectory)) {
            Files.createDirectories(templatesDirectory);
        }
        Path defaultTemplate = templatesDirectory.resolve("default" + XillEnvironment.ROBOT_TEMPLATE_EXTENSION);
        if (!Files.exists(defaultTemplate)) {
            try (InputStream defaultTemplateRes = getClass().getResource("/templates/default" + XillEnvironment.ROBOT_TEMPLATE_EXTENSION).openStream()) {
                Files.copy(defaultTemplateRes, defaultTemplate);
            }
        }
        Templater.CONFIG.setDirectoryForTemplateLoading(templatesDirectory.toFile());
        Templater.CONFIG.setDefaultEncoding("UTF-8");
        Templater.CONFIG.setTemplateExceptionHandler((exception, env, out) -> {
            try {
                out.write("[ERROR: " + exception.getBlamedExpressionString() + "]");
            } catch (IOException e) {
                throw new TemplateException("Failed to print error message. Cause: " + e, env);
            }
        });
    }

    /**
     * The list of currently available templates (as defined by {@link XillEnvironment#ROBOT_TEMPLATE_EXTENSION} files being in the template folder).
     *
     * @return the list of available templates
     */
    public List<String> getTemplateNames() throws IOException {
        return Files.list(templatesDirectory)
                .map(Path::getFileName)
                .map(Path::toString)
                .map(String::toLowerCase)
                .filter(name -> name.endsWith(XillEnvironment.ROBOT_TEMPLATE_EXTENSION.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Returns a default model with some already initialized key-value pairs.
     * Currently:
     * <ul>
     * <li>{@code date}: the current date</li>
     * <li>{@code userName}: the current logged in user</li>
     * </ul>
     *
     * @return a default template model
     */
    public static Map<String, Object> getDefaultModel() {
        Map<String, Object> defaultModel = new HashMap<>();
        defaultModel.put("date", LocalDate.now());
        defaultModel.put("userName", System.getProperty("user.name"));
        return defaultModel;
    }

    /**
     * Renders a template using a model and writes it in the provided output file.
     *
     * @param templateName the template to render
     * @param model        the model to use during the rendering
     * @param output       the file in which the template will be rendered
     * @return the output file
     * @throws IOException       if permissions are not sufficient, files are missing or any other IO problem
     * @throws TemplateException if the template couldn't be processed
     */
    public Path render(String templateName, Map<String, Object> model, Path output) throws IOException, TemplateException {
        if (templateName != null) {
            Template template = Templater.CONFIG.getTemplate(templateName);
            try(OutputStreamWriter out = new FileWriter(output.toFile())) {
                template.process(model, out);
            }
        } else {
            FileUtils.write(output.toFile(), "", false);
        }
        return output;
    }

}