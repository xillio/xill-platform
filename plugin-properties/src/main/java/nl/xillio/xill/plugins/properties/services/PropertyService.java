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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.biesaart.utils.Log;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.plugins.properties.PropertiesLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This service is responsible for resolving properties from a context and a property name.
 *
 * @author Thomas Biesaart
 */
@Singleton
public class PropertyService implements PropertiesLoader {
    private static final String DEFAULTS_FILE = "defaults.properties";
    private static final String PROPERTIES_FILE = "xill.properties";
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("(?<!\\$)\\$\\{([^{}]+)}");
    private static final Logger LOGGER = Log.get();
    private final Properties defaultGlobalProperties;
    private final ContextPropertiesResolver contextPropertiesResolver;
    private final Map<UUID, PropertiesCache> propertiesCache = new HashMap<>();

    @Inject
    public PropertyService(@Named("defaults") Properties defaultGlobalProperties, ContextPropertiesResolver contextPropertiesResolver) {
        this.defaultGlobalProperties = defaultGlobalProperties;
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
                context.getRootLogger().debug("When requesting '{}' a missing property '{}' was used", name, variable);
                value = "";
            }
            propertyValue = matcher.replaceFirst(value.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\$"));
            matcher.reset(propertyValue);
        }

        return propertyValue;
    }

    synchronized String getProperty(String name, String defaultValue, ConstructContext context) {
        PropertiesCache propertiesCache = getPropertiesCache(context);

        // First we check the project and context defaults
        String result = propertiesCache
                .getProjectProperty(name)
                .orElseGet(
                        () -> propertiesCache.getContextProperty(name, context).orElse(defaultValue)
                );

        if (result != null) {
            return result;
        }

        return propertiesCache.getDefaultProperty(name, context).orElse(null);
    }

    private PropertiesCache getPropertiesCache(ConstructContext context) {
        return propertiesCache.computeIfAbsent(context.getCompilerSerialId(), id -> {
            PropertiesCache cache = new PropertiesCache(
                    loadProperties(PROPERTIES_FILE, context, null),
                    contextPropertiesResolver,
                    this,
                    defaultGlobalProperties
            );
            context.addRobotStoppedListener(event -> propertiesCache.remove(id));
            return cache;
        });
    }

    @Override
    public Properties loadProperties(String resourceFolder, ConstructContext context, Properties defaults) {
        if (resourceFolder == null || resourceFolder.isEmpty()) {
            throw new IllegalArgumentException("An empty resource folder was provided");
        }
        String resource = resourceFolder + "/" + DEFAULTS_FILE;
        Properties result = new Properties(defaults);
        try {
            InputStream inputStream = context.getResourceLoader().getResourceAsStream(resource);

            if (inputStream != null) {
                try (InputStreamReader stream = new InputStreamReader(inputStream, Charset.defaultCharset())) {
                    result.load(stream);
                }
            }

        } catch (IOException e) {
            context.getRootLogger().warn("Failed to load properties from " + resource + "\nReason: " + e.getMessage(), e);
        }
        return result;
    }
}
