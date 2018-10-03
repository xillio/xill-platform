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
import com.google.inject.Injector;
import nl.xillio.engine.GetEntityRequestParameters;
import nl.xillio.engine.configuration.Configuration;
import nl.xillio.engine.connector.Connector;
import nl.xillio.engine.connector.EntityQueryResult;
import nl.xillio.engine.model.XDIP;
import nl.xillio.engine.model.converter.Converter;
import nl.xillio.xill.TestUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XillioEngineServiceTest extends TestUtils {
    private XillioEngineService service;

    @BeforeTest
    public void prepare() {
        Injector injector = Guice.createInjector();
        service = injector.getInstance(XillioEngineService.class);
    }

    @Test
    public void testGetConnector() throws ClassNotFoundException {
        Connector connector = service.createConnectorInstance("dropbox");
    }

    @Test
    public void testGetConfiguration() throws ClassNotFoundException {
        Connector connector = service.createConnectorInstance("dropbox");

        Map<String, Object> configurationProperties = new HashMap<>();
        configurationProperties.put("token", "asdf");

        Configuration configuration = service.createConfiguration(connector, configurationProperties);

        GetEntityRequestParameters requestParameters = new GetEntityRequestParameters();

        EntityQueryResult result = connector.getEntity(XDIP.encode("/"), configuration, requestParameters);
    }

    @Test
    public void testReflections() {
        Reflections r = new Reflections("nl.xillio.engine.connector.dropbox",
                new SubTypesScanner(false));

        Set<?> c = r.getSubTypesOf(Converter.class);
    }
}
