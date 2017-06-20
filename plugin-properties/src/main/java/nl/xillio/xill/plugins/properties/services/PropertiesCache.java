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
package nl.xillio.xill.plugins.properties.services;


import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.plugins.properties.PropertiesLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class PropertiesCache {
    private final Properties projectProperties;
    private final ContextPropertiesResolver contextPropertiesResolver;
    private final PropertiesLoader propertiesLoader;
    private final Properties defaultGlobalProperties;
    private final Map<String, Properties> cache = new HashMap<>();

    public PropertiesCache(Properties projectProperties, ContextPropertiesResolver contextPropertiesResolver, PropertiesLoader propertiesLoader, Properties defaultGlobalProperties) {
        this.projectProperties = projectProperties;
        this.contextPropertiesResolver = contextPropertiesResolver;
        this.propertiesLoader = propertiesLoader;
        this.defaultGlobalProperties = defaultGlobalProperties;
    }

    public Optional<String> getProjectProperty(String name) {
        return Optional.ofNullable(projectProperties.getProperty(name));
    }

    public Optional<String> getContextProperty(String name, ConstructContext context) {
        return contextPropertiesResolver.resolve(name, context);
    }

    public Optional<String> getDefaultProperty(String name, ConstructContext context) {
        return Optional.ofNullable(
                getDefaultProperties(context.getRobotID().getResourcePath(), context).getProperty(name)
        );
    }

    private Properties getDefaultProperties(String robotResourcePath, ConstructContext context) {
        String resourceFolder = getParent(robotResourcePath);
        return cache.computeIfAbsent(resourceFolder, p -> loadDefaultProperty(resourceFolder, context));
    }

    private Properties loadDefaultProperty(String resourceFolder, ConstructContext context) {

        if (resourceFolder.isEmpty()) {
            // This is the root folder
            return propertiesLoader.loadProperties(
                    PropertyService.DEFAULTS_FILE,
                    context,
                    defaultGlobalProperties
            );
        } else {
            String resource = resourceFolder + "/" + PropertyService.DEFAULTS_FILE;
            return propertiesLoader.loadProperties(
                    resource,
                    context,
                    getDefaultProperties(resourceFolder, context)
            );
        }
    }

    private String getParent(String resource) {
        int slashIndex = Math.max(
                resource.lastIndexOf('/'),
                resource.lastIndexOf('\\')
        );

        if (slashIndex == -1) {
            return "";
        }

        return resource.substring(0, slashIndex);
    }
}
