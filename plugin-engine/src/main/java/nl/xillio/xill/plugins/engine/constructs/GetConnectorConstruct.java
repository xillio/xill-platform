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
package nl.xillio.xill.plugins.engine.constructs;

import com.google.inject.Inject;
import nl.xillio.engine.configuration.Configuration;
import nl.xillio.engine.connector.Connector;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.engine.model.ConnectorMetadataExpression;
import nl.xillio.xill.plugins.engine.services.XillioEngineService;

public class GetConnectorConstruct extends Construct {
    @Inject
    private XillioEngineService engineService;

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (connectorName, connectorConfiguration) -> process(engineService, connectorName, connectorConfiguration),
                new Argument("connectorName", ATOMIC),
                new Argument("configuration", OBJECT));
    }

    public static MetaExpression process(XillioEngineService engineService,
                                         MetaExpression connectorName,
                                         MetaExpression configurationExpression) {
        try {
            Connector connector = engineService.createConnectorInstance(connectorName.getStringValue());
            Configuration configuration = engineService.createConfiguration(connector, configurationExpression.getValue());

            ConnectorMetadataExpression meta = new ConnectorMetadataExpression(connector, configuration);
            MetaExpression result = fromValue("Connector: " + connectorName.getStringValue());
            result.storeMeta(meta);

            return result;

        } catch(ClassNotFoundException e) {
            throw new RuntimeException("class not found!", e);
        }
    }
}
