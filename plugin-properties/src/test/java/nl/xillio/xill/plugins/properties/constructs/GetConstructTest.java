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
package nl.xillio.xill.plugins.properties.constructs;

import nl.xillio.events.EventHost;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.NullDebugger;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.io.ResourceLoader;
import nl.xillio.xill.plugins.properties.services.ContextPropertiesResolver;
import nl.xillio.xill.plugins.properties.services.PropertyService;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;


public class GetConstructTest extends TestUtils {

    /**
     * This test will test normal usage without loading files.
     */
    @Test
    public void testNormalUsageNoFiles() throws IOException {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getResourceAsStream(anyString())).thenReturn(null);

        GetConstruct construct = new GetConstruct(
                new PropertyService(new Properties(), new ContextPropertiesResolver())
        );

        MetaExpression notFound = process(resourceLoader, construct, fromValue("propertyNotExists"));
        assertEquals(notFound, NULL);

        MetaExpression notFoundButDefault = process(resourceLoader, construct, fromValue("notExists"), fromValue("Hello World"));
        assertEquals(notFoundButDefault.getStringValue(), "Hello World");
    }

    @Test
    public void testNormalUsageNoFilesButDefaults() throws IOException {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getResourceAsStream(anyString())).thenReturn(null);

        Properties properties = new Properties();
        properties.put("defaultValue", "YES");

        GetConstruct construct = new GetConstruct(
                new PropertyService(properties, new ContextPropertiesResolver())
        );

        MetaExpression result = process(resourceLoader, construct, fromValue("defaultValue"));
        assertEquals(result.getStringValue(), "YES");
    }

    protected MetaExpression process(ResourceLoader resourceLoader, Construct construct, MetaExpression... arguments) {
        RobotID robotId = RobotID.dummyRobot();
        return ConstructProcessor.process(
                construct.prepareProcess(
                        new ConstructContext(
                                Paths.get("."),
                                robotId,
                                robotId,
                                construct,
                                new NullDebugger(),
                                UUID.randomUUID(),
                                new EventHost<>(),
                                new EventHost<>(),
                                resourceLoader)
                ),
                arguments
        );
    }
}
