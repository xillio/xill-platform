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
package nl.xillio.xill.api;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * A utility class to create environments.
 */
public class XillLoader {
    private XillLoader() {
        // None shall have
    }
    /**
     * Loads a XillEnvironment from a specific folder.
     *
     * @param coreFolder the folder
     * @return the environment
     * @throws IOException if no environment could be found
     */
    public static XillEnvironment getEnv(Path coreFolder) throws IOException {
        // Try to load the XillEnvironment from the classpath first
        XillEnvironment environment = loadXillEnvironment(null);
        if (environment != null) {
            return environment;
        }

        return getEnv(coreFolder, XillEnvironment.class.getClassLoader());
    }

    /**
     * Loads a XillEnvironment from a specific folder with a specified classloader as a parent.
     *
     * @param coreFolder        the folder
     * @param parentClassLoader the parent classloader
     * @return the environment
     * @throws IOException if no environment could be found
     */
    public static XillEnvironment getEnv(Path coreFolder, ClassLoader parentClassLoader) throws IOException {
        JarFinder finder = new JarFinder();
        Files.walkFileTree(coreFolder, finder);

        for (Path jarFile : finder.getJarFiles()) {
            if (jarFile.toString().contains("processor")) {
                // This check if to improve performance. We only load from processor jars
                URL url = jarFile.toUri().toURL();
                URLClassLoader classLoader = new URLClassLoader(new URL[]{url}, parentClassLoader);
                XillEnvironment xillEnvironment = loadXillEnvironment(classLoader);
                if (xillEnvironment != null) {
                    return xillEnvironment;
                }
            }
        }

        throw new NoSuchFileException("No XillEnvirionment implementation found in " + coreFolder);
    }

    private static XillEnvironment loadXillEnvironment(ClassLoader classLoader) {
        ServiceLoader<XillEnvironment> loader = ServiceLoader.load(XillEnvironment.class, classLoader);
        for (XillEnvironment environment : loader) {
            return environment;
        }
        return null;
    }

    /**
     * This class will collect a list of all .jar files.
     */
    private static class JarFinder extends SimpleFileVisitor<Path> {
        private static final PathMatcher JAR_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**.jar");
        private final List<Path> jarFiles = new ArrayList<>();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (JAR_MATCHER.matches(file)) {
                jarFiles.add(file);
            }
            return super.visitFile(file, attrs);
        }

        private List<Path> getJarFiles() {
            return jarFiles;
        }
    }
}
