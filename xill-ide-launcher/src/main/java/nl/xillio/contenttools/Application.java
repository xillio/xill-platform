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
package nl.xillio.contenttools;

import javafx.stage.Stage;
import me.biesaart.utils.Log;
import nl.xillio.plugins.ContenttoolsPlugin;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.XillLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * This is the main class of the Xill IDE application
 */
public class Application extends javafx.application.Application {
    private static final Logger LOGGER = Log.get();
    private static final Path PLUGIN_FOLDER = Paths.get("plugins");
    private static XillEnvironment environment;

    private static List<String> args;


    /**
     * Main method
     *
     * @param args the main arguments to run the program
     * @throws IOException if the plugin folder cannot be created
     */
    public static void main(final String... args) throws IOException {
        LOGGER.info("Starting host application");
        Files.createDirectories(PLUGIN_FOLDER);
        environment = XillLoader.getEnv(PLUGIN_FOLDER);

        try {
            SingleInstanceHandler.start();
            launch(args);
        } catch (IOException e) {
            LOGGER.debug("An instance is already running");
            SingleInstanceHandler.notifyListeners(args);
        }
    }

    @Override
    public void start(final Stage stage) throws Exception {
        LOGGER.info("Loading IDE");
        // Try to load the IDE from the classpath first
        if (loadIDE(stage, null)) {
            return;
        }

        IDEJarFinder finder = new IDEJarFinder();
        Files.walkFileTree(PLUGIN_FOLDER, finder);

        args = getParameters().getUnnamed();

        for (Path file : finder.getMatches()) {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toUri().toURL()});
            if (loadIDE(stage, classLoader)) {
                return;
            }
        }

        throw new IOException("Could not find implementation of ide");
    }

    private boolean loadIDE(Stage stage, URLClassLoader classLoader) {
        ServiceLoader<ContenttoolsPlugin> loader = ServiceLoader.load(ContenttoolsPlugin.class, classLoader);

        Iterator<ContenttoolsPlugin> iterator = loader.iterator();
        if (iterator.hasNext()) {
            ContenttoolsPlugin ide = iterator.next();
            ide.start(stage, environment);

            SingleInstanceHandler.addListener(message -> Arrays.stream(message)
                    .filter(e -> e.endsWith(XillEnvironment.ROBOT_EXTENSION))
                    .forEach(ide.getSingleInstanceHandler()));
            getParameters().getUnnamed().stream()
                    .filter(e -> e.endsWith(XillEnvironment.ROBOT_EXTENSION))
                    .forEach(ide.getSingleInstanceHandler());
            return true;
        }
        return false;
    }

    private static class IDEJarFinder extends SimpleFileVisitor<Path> {
        private static final PathMatcher JAR_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**.jar");
        private final List<Path> matches = new ArrayList<>();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (JAR_MATCHER.matches(file) && file.toString().contains("ide")) {
                matches.add(file);
            }
            return super.visitFile(file, attrs);
        }

        public List<Path> getMatches() {
            return matches;
        }
    }
}
