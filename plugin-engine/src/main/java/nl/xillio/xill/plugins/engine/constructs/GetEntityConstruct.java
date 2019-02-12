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
package nl.xillio.xill.plugins.engine.constructs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import nl.xillio.engine.GetEntityRequestParameters;
import nl.xillio.engine.ProjectionScope;
import nl.xillio.engine.configuration.Configuration;
import nl.xillio.engine.connector.Connector;
import nl.xillio.engine.connector.EntityQueryResult;
import nl.xillio.engine.model.XDIP;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.engine.model.ConnectorMetadataExpression;
import nl.xillio.xill.plugins.engine.services.XillioEngineService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetEntityConstruct extends Construct {
    @Inject
    private XillioEngineService engineService;

    @Inject
    private ObjectMapper objectMapper;

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (connector, xdip, scopes) -> process(engineService, objectMapper, connector, xdip, scopes),
                new Argument("connector", ATOMIC),
                new Argument("xdip", ATOMIC),
                new Argument("scopes", LIST));
    }

    public static MetaExpression process(XillioEngineService engineService,
                                         ObjectMapper objectMapper,
                                         MetaExpression connectorExpression,
                                         MetaExpression xdipExpression,
                                         MetaExpression scopesExpression) {
        try {
            Connector connector = connectorExpression.getMeta(ConnectorMetadataExpression.class).getConnector();
            Configuration configuration = connectorExpression.getMeta(ConnectorMetadataExpression.class).getConfiguration();

            List<ProjectionScope> scopeList = ((List<MetaExpression>) scopesExpression.getValue()).stream()
                    .map(e -> e.getStringValue())
                    .map(s -> ProjectionScope.get(s).get())
                    .collect(Collectors.toList());

            GetEntityRequestParameters parameters = new GetEntityRequestParameters();
            parameters.setProjectionScopes(scopeList.toArray(new ProjectionScope[]{}));

            EntityQueryResult result = connector.getEntity(XDIP.encode(xdipExpression.getStringValue()),
                    configuration,
                    parameters);

            String serialized = objectMapper.writeValueAsString(result);
            Map<String, Object> mapResult = objectMapper.readValue(serialized, Map.class);

            return MetaExpression.parseObject(mapResult);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
