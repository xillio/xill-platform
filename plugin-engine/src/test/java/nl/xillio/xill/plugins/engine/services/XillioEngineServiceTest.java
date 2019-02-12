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

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import nl.xillio.engine.GetEntityRequestParameters;
import nl.xillio.engine.ProjectionScope;
import nl.xillio.engine.configuration.Configuration;
import nl.xillio.engine.connector.Connector;
import nl.xillio.engine.connector.EntityQueryResult;
import nl.xillio.engine.connector.dropbox.DropboxConnectorConfiguration;
import nl.xillio.engine.model.XDIP;
import nl.xillio.engine.model.converter.Converter;
import nl.xillio.engine.services.ConverterScanner;
import nl.xillio.xill.plugins.engine.XillioEngineXillPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@org.testng.annotations.Guice(modules = XillioEngineXillPlugin.class)
public class XillioEngineServiceTest {
    private XillioEngineService service;

    @Inject
    private XillioEngineXillPlugin xillioEngineXillPlugin;

    @BeforeTest
    public void prepare() {
        service = xillioEngineXillPlugin.getInjector().getInstance(XillioEngineService.class);
    }

    @Test
    public void testGetConnector() throws ClassNotFoundException {
        Connector connector = service.createConnectorInstance("dropbox");
    }

    @Test
    public void testGetConfiguration() throws ClassNotFoundException {
        Connector connector = service.createConnectorInstance("dropbox");

        Map<String, Object> configurationProperties = new HashMap<>();
        configurationProperties.put("token", "8Y23tH489uAAAAAAAAACWCNDacce1MbHfo_kkbJVbSc7gWzCLxmpSiar1WpV---L");

        Configuration configuration = service.createConfiguration(connector, configurationProperties);

        GetEntityRequestParameters requestParameters = new GetEntityRequestParameters();
        requestParameters.setProjectionScopes(ProjectionScope.CHILDREN);

        EntityQueryResult result = connector.getEntity(XDIP.encode("/"), configuration, requestParameters);
    }

    @Test
    public void testReflections() {
        Reflections r = new Reflections("nl.xillio.engine.connector.dropbox",
                "nl.xillio.engine.model.converter.shared",
                "nl.xillio.engine.model.converter",
                new SubTypesScanner(false));

        Set<? extends Class<? extends Converter>> c = r.getSubTypesOf(Converter.class);

        Injector injector = Guice.createInjector();

        Object res = injector.getInstance(c.iterator().next());
    }

    @Test
    public void testScanner() {
        ConverterScanner scanner = xillioEngineXillPlugin.getInjector().getInstance(ConverterScanner2.class);

        List<Converter<DropboxConnectorConfiguration>> res = scanner.scanConverters(DropboxConnectorConfiguration.class);
    }
}
