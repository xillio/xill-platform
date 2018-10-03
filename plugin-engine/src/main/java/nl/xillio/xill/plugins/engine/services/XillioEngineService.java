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
package nl.xillio.xill.plugins.engine.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Injector;
import nl.xillio.engine.configuration.Configuration;
import nl.xillio.engine.connector.Connector;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;

public class XillioEngineService {
    private final Injector injector;
    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    public XillioEngineService(Injector injector) {
        this.injector = injector;
    }

    public Connector createConnectorInstance(String connectorName) throws ClassNotFoundException {
        String lowercaseConnectorName = connectorName.toLowerCase();
        String titleCaseConnectorName = StringUtils.capitalize(connectorName.toLowerCase());
        String fullyQualifiedConnectorClassName = "nl.xillio.engine.connector." +
                lowercaseConnectorName + "." + titleCaseConnectorName + "Connector";
        Class<? extends Connector> connectorClass = Class.forName(fullyQualifiedConnectorClassName).asSubclass(Connector.class);

        return injector.getInstance(connectorClass);
    }

    public Configuration createConfiguration(Connector connector, Map<String, ?> configuration) throws ClassNotFoundException {
        String fullyQualifiedConfigurationClassName = connector.getClass().getCanonicalName() + "Configuration";
        Class<? extends Configuration> configurationClass = Class
                .forName(fullyQualifiedConfigurationClassName)
                .asSubclass(Configuration.class);

        return toConfiguration(configuration, configurationClass);
    }

    public Configuration toConfiguration(Map<String, ?> configurationFields, Class<? extends Configuration> targetClass) {
        // Instantiate the default configuration
        Configuration configuration = injector.getInstance(targetClass);

        // Build the configuration
        marshall(configuration, configurationFields);
        return configuration;
    }

    private void marshall(Configuration configuration, Map<String, ?> configurationFields) {
        try {
            mapper.readerForUpdating(configuration).readValue(
                    mapper.writeValueAsString(
                            configurationFields
                    ));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to marshall json data into '" + configuration.getClass() + "': " + e.getMessage(), e);
        }
    }
}
