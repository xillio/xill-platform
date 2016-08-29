/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.plugins.properties.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.biesaart.utils.Log;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.ConstructContext;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This service is responsible for resolving properties from a context and a property name.
 *
 * @author Thomas Biesaart
 */
@Singleton
public class PropertyService {
    private static final String DEFAULTS_FILE = "defaults.properties";
    private static final String PROPERTIES_FILE = "xill.properties";
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("(?<!\\$)\\$\\{([^{}]+)}");
    private static final Logger LOGGER = Log.get();
    private final Properties defaultGlobalProperties;
    private final Map<PathPair, Properties> defaultsPropertiesMap = new HashMap<>();
    private final Map<Path, Properties> projectPropertiesMap = new HashMap<>();
    private final FileSystemAccess fileSystemAccess;
    private final ContextPropertiesResolver contextPropertiesResolver;

    @Inject
    public PropertyService(@Named("defaults") Properties defaultGlobalProperties, FileSystemAccess fileSystemAccess, ContextPropertiesResolver contextPropertiesResolver) {
        this.defaultGlobalProperties = defaultGlobalProperties;
        this.fileSystemAccess = fileSystemAccess;
        this.contextPropertiesResolver = contextPropertiesResolver;
    }

    public String getFormattedProperty(String name, String defaultValue, ConstructContext context) {
        String propertyValue = getProperty(name, defaultValue, context);

        if (propertyValue == null) {
            return null;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(propertyValue);
        while (matcher.find()) {
            String variable = matcher.group(1);
            String value = getFormattedProperty(variable, null, context);
            if (value == null) {
                value = "";
            }
            propertyValue = matcher.replaceFirst(value.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\$"));
            matcher.reset(propertyValue);
        }

        return propertyValue;
    }

    synchronized String getProperty(String name, String defaultValue, ConstructContext context) {
        // First we check the project
        Path projectFolder = context.getRobotID().getProjectPath().toPath();
        if (!projectPropertiesMap.containsKey(projectFolder)) {
            projectPropertiesMap.put(
                    projectFolder,
                    loadPropertiesFromFile(
                            projectFolder.resolve(PROPERTIES_FILE),
                            null,
                            context.getRootLogger()
                    )
            );
        }
        Properties projectProperties = projectPropertiesMap.get(projectFolder);
        if (projectProperties.containsKey(name)) {
            // We have a project override
            return projectProperties.getProperty(name);
        }

        // Check if this variable is available in the context resolver or a default value is provided
        String contextProperty = contextPropertiesResolver.resolve(name, context).orElse(defaultValue);

        if (contextProperty != null) {
            return contextProperty;
        }

        // Seems like we have to load a default
        Properties properties = loadDefaultProperties(new PathPair(context.getRobotID()), context.getRootLogger());

        return properties.getProperty(name);
    }

    private Properties loadDefaultProperties(PathPair pathPair, Logger warningLogger) {
        if (defaultsPropertiesMap.containsKey(pathPair)) {
            // Already cached
            return defaultsPropertiesMap.get(pathPair);
        }

        // We need to load the properties
        Properties properties;
        if (pathPair.isProjectFolder()) {
            LOGGER.info("Loading properties for project: {}", pathPair.projectFolder);
            // This is the project root. Load from defaults.
            properties = loadPropertiesFromFile(
                    pathPair.projectFolder.resolve(DEFAULTS_FILE),
                    defaultGlobalProperties,
                    warningLogger
            );
        } else if (pathPair.isInProject()) {
            // This is a folder from within the project so load parent folder first
            Properties parentDefaults = loadDefaultProperties(
                    pathPair.getParent(),
                    warningLogger
            );

            properties = loadPropertiesFromFile(
                    pathPair.robotFolder.resolve(DEFAULTS_FILE),
                    parentDefaults,
                    warningLogger
            );
        } else {
            // This is a single robot called using callbot or concurrency and it exists outside of the project
            // We load with defaults only
            properties = new Properties(defaultGlobalProperties);
        }
        defaultsPropertiesMap.put(pathPair, properties);
        return properties;
    }

    private Properties loadPropertiesFromFile(Path file, Properties defaults, Logger logger) {
        Properties result = new Properties(defaults);
        if (!fileSystemAccess.exists(file)) {
            // If the file does not exist we will not load them
            return result;
        }
        try (InputStream stream = fileSystemAccess.read(file)) {
            result.load(stream);
        } catch (IOException e) {
            logger.warn("Failed to load properties from " + file + "\nReason: " + e.getMessage(), e);
        }

        return result;
    }

    public synchronized void clean() {
        projectPropertiesMap.clear();
        defaultsPropertiesMap.clear();
    }

    private class PathPair {
        private final Path projectFolder;
        private final Path robotFolder;

        private PathPair(RobotID robotID) {
            this(robotID.getProjectPath().toPath(), robotID.getPath().toPath().getParent());
        }

        private PathPair(Path projectFolder, Path robotFolder) {
            this.projectFolder = projectFolder.toAbsolutePath().normalize();
            this.robotFolder = robotFolder.toAbsolutePath().normalize();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof PathPair) {
                PathPair other = (PathPair) obj;
                return projectFolder.equals(other.projectFolder) && robotFolder.equals(other.robotFolder);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(projectFolder, robotFolder);
        }

        private boolean isProjectFolder() {
            return projectFolder.equals(robotFolder);
        }

        private boolean isInProject() {
            return robotFolder.startsWith(projectFolder);
        }

        private PathPair getParent() {
            return new PathPair(projectFolder, robotFolder.getParent());
        }
    }

}
