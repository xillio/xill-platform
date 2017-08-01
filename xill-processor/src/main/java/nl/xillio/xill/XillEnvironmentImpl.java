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
package nl.xillio.xill;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import me.biesaart.utils.Log;
import nl.xillio.plugins.PluginLoadFailure;
import nl.xillio.plugins.XillPlugin;
import nl.xillio.util.XillioHomeFolder;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.XillProcessor;
import nl.xillio.xill.api.XillThreadFactory;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.debugging.XillDebugger;
import nl.xillio.xill.loaders.AbstractRobotLoader;
import nl.xillio.xill.loaders.DirectoryRobotLoader;
import nl.xillio.xill.loaders.ArchiveRobotLoader;
import nl.xillio.xill.services.ProgressTracker;
import nl.xillio.xill.services.inject.DefaultInjectorModule;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * This class is responsible for loading the processor and it's plugins.
 *
 * @author Thomas Biesaart
 */
public class XillEnvironmentImpl implements XillEnvironment {
    private static final Path HOME_PLUGIN_DIR = Paths.get(XillioHomeFolder.forXill3().toURI()).resolve("plugins");
    private static final Logger LOGGER = Log.get();
    private boolean loadHome = true;
    private final List<Path> folders = new ArrayList<>();
    private Injector rootInjector;
    private Map<String, XillPlugin> loadedPlugins = new HashMap<>();
    private List<PluginLoadFailure> invalidPlugins = new ArrayList<>();
    private boolean needLoad = true;
    private XillThreadFactory xillThreadFactory;
    private ProgressTracker progressTracker;

    @Override
    public XillEnvironment setLoadHomeFolder(boolean value) {
        loadHome = value;
        return this;
    }

    @Override
    public XillEnvironment addFolder(Path path) throws IOException {
        if (Files.exists(path)) {
            folders.add(path);
        }
        return this;
    }

    @Override
    public XillEnvironment setRootInjector(Injector injector) {
        rootInjector = injector;
        return this;
    }

    public XillEnvironment setXillThreadFactory(XillThreadFactory xillThreadFactory) {
        this.xillThreadFactory = xillThreadFactory;
        return this;
    }

    @Override
    public XillEnvironment loadPlugins() throws IOException {
        if (rootInjector == null) {
            rootInjector = Guice.createInjector();
        }

        List<Path> folders = new ArrayList<>(this.folders);
        if (loadHome && Files.exists(HOME_PLUGIN_DIR)) {
            folders.add(HOME_PLUGIN_DIR);
        }
        loadClasspathPlugins();
        loadPlugins(folders);
        needLoad = false;

        // Create the default thread factory when one has not been set
        if (xillThreadFactory == null)
            xillThreadFactory = new XillThreadFactoryImpl();

        List<Module> modules = new ArrayList<>(loadedPlugins.values());
        modules.add(new DefaultInjectorModule(this, xillThreadFactory));
        Injector configuredInjector = rootInjector.createChildInjector(modules);

        LOGGER.info("Injecting plugin members");
        // Inject members
        loadedPlugins.values().forEach(configuredInjector::injectMembers);

        LOGGER.info("Loading constructs");
        // Load constructs
        loadedPlugins.values().forEach(XillPlugin::initialize);

        progressTracker = configuredInjector.getInstance(ProgressTracker.class);

        return this;
    }

    @Override
    public XillProcessor buildProcessor(Path workingDirectory, String robot, Path... robotPath) throws IOException {
        return buildProcessor(workingDirectory, robot, new XillDebugger(), robotPath);
    }

    @Override
    public XillProcessor buildProcessor(Path workingDirectory, RobotID robotId, Path... robotPath) throws IOException {
        return buildProcessor(workingDirectory, robotId, new XillDebugger(), robotPath);
    }

    @Override
    public XillProcessor buildProcessor(Path workingDirectory, RobotID robotID, Debugger debugger, Path... robotPath) throws IOException {
        return buildProcessor(
                workingDirectory,
                robotID,
                debugger,
                buildRobotLoader(workingDirectory, robotPath)
        );
    }

    @Override
    public XillProcessor buildProcessor(Path workingDirectory, String robot, Debugger debugger, Path... robotPath) throws IOException {
        AbstractRobotLoader robotLoader = buildRobotLoader(workingDirectory, robotPath);

        // Try to load by fqn.
        URL resource = robotLoader.getRobot(robot);
        String resourcePath = RobotID.qualifiedNameToPath(robot);

        // If loading by fqn failed, try loading by path.
        if (resource == null) {
            resource = robotLoader.getResource(robot);
            resourcePath = robot;
        }

        // Check if no robot was found.
        if (resource == null) {
            throw new NoSuchFileException("'" + robot + "' could not be resolved");
        }

        RobotID robotID = new RobotID(resource, resourcePath);

        return buildProcessor(
                workingDirectory,
                robotID,
                debugger,
                robotLoader
        );
    }

    private XillProcessor buildProcessor(Path workingDirectory, RobotID robotID, Debugger debugger, AbstractRobotLoader robotLoader) throws IOException {
        if (needLoad) {
            loadPlugins();
        }

        return new nl.xillio.xill.XillProcessor(
                workingDirectory.toAbsolutePath(),
                robotID,
                robotLoader,
                getPlugins(),
                debugger
        );
    }

    public AbstractRobotLoader buildRobotLoader(Path workingDirectory, Path... robotPath) throws IOException {
        AbstractRobotLoader robotLoader = new DirectoryRobotLoader(null, workingDirectory);
        for (Path path : robotPath) {
            if (Files.isRegularFile(path)) {
                robotLoader = new ArchiveRobotLoader(robotLoader, path.toAbsolutePath());
            } else {
                robotLoader = new DirectoryRobotLoader(robotLoader, path.toAbsolutePath());
            }
        }
        return robotLoader;
    }

    @Override
    public List<XillPlugin> getPlugins() {
        return new ArrayList<>(loadedPlugins.values());
    }

    @Override
    public List<PluginLoadFailure> getMissingLicensePlugins() {
        return Collections.unmodifiableList(invalidPlugins);
    }

    @Override
    public void close() {
        getPlugins().forEach(XillPlugin::close);
    }

    private void loadClasspathPlugins() {
        loadPlugins(ServiceLoader.load(XillPlugin.class));
    }

    private void loadPlugins(Iterable<Path> folders) throws IOException {
        for (Path folder : folders) {
            assertFolderExists(folder);

            Processor processor = new Processor();
            Files.walkFileTree(folder, processor);
            for (Path jarFile : processor.getJarFiles()) {
                URL url = jarFile.toUri().toURL();
                URLClassLoader classLoader = new URLClassLoader(new URL[]{url}, XillPlugin.class.getClassLoader());
                ServiceLoader<XillPlugin> serviceLoader = ServiceLoader.load(XillPlugin.class, classLoader);
                loadPlugins(serviceLoader);
            }
        }
    }

    private void loadPlugins(ServiceLoader<XillPlugin> serviceLoader) {
        for (Iterator<XillPlugin> pluginIterator = serviceLoader.iterator(); pluginIterator.hasNext(); ) {
            try {
                XillPlugin plugin = pluginIterator.next();
                String name = plugin.getName();
                if (!loadedPlugins.containsKey(name)) {
                    LOGGER.info("Found plugin {}", name);
                    loadedPlugins.put(name, plugin);
                }
            } catch (ServiceConfigurationError error) {
                LOGGER.warn("Can not load plugin", error);
                invalidPlugins.add(PluginLoadFailure.parse(error.getCause().getMessage()));
            }
        }

    }

    private void assertFolderExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new NoSuchFileException("No folder found at " + path);
        }

        if (!Files.isDirectory(path)) {
            throw new NotDirectoryException(path + " is not a directory");
        }

    }

    /**
     * This class will save a list of all .jar files.
     */
    private static class Processor extends SimpleFileVisitor<Path> {
        private static final PathMatcher JAR_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**.jar");
        private final List<Path> jarFiles = new ArrayList<>();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (JAR_MATCHER.matches(file)) {
                jarFiles.add(file);
            }
            return super.visitFile(file, attrs);
        }

        public List<Path> getJarFiles() {
            return jarFiles;
        }
    }

    /**
     * Gets a {@link ProgressTracker} object.
     *
     * @return ProgressTracker object
     */
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }
}
