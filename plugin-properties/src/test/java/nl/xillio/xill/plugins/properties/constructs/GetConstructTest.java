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

import nl.xillio.xill.plugins.properties.services.ContextPropertiesResolver;
import nl.xillio.xill.plugins.properties.services.FileSystemAccess;
import nl.xillio.xill.plugins.properties.services.PropertyService;
import nl.xillio.events.EventHost;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.NullDebugger;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Properties;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;


public class GetConstructTest extends TestUtils {

    /**
     * This test will test normal usage without loading files.
     */
    @Test
    public void testNormalUsageNoFiles() {
        FileSystemAccess fileSystemAccess = mock(FileSystemAccess.class);
        when(fileSystemAccess.exists(any())).thenReturn(false);

        GetConstruct construct = new GetConstruct(
                new PropertyService(new Properties(), fileSystemAccess, new ContextPropertiesResolver())
        );

        MetaExpression notFound = process(construct, fromValue("propertyNotExists"));
        assertEquals(notFound, NULL);

        MetaExpression notFoundButDefault = process(construct, fromValue("notExists"), fromValue("Hello World"));
        assertEquals(notFoundButDefault.getStringValue(), "Hello World");
    }

    @Test
    public void testNormalUsageNoFilesButDefaults() {
        FileSystemAccess fileSystemAccess = mock(FileSystemAccess.class);
        when(fileSystemAccess.exists(any())).thenReturn(false);

        Properties properties = new Properties();
        properties.put("defaultValue", "YES");

        GetConstruct construct = new GetConstruct(
                new PropertyService(properties, fileSystemAccess, new ContextPropertiesResolver())
        );

        MetaExpression result = process(construct, fromValue("defaultValue"));
        assertEquals(result.getStringValue(), "YES");
    }

    @Override
    protected MetaExpression process(Construct construct, MetaExpression... arguments) {
        RobotID robotId = RobotID.getInstance(new File("./libs/test.xill"), new File("."));
        return ConstructProcessor.process(
                construct.prepareProcess(
                        new ConstructContext(
                                robotId,
                                robotId,
                                construct,
                                new NullDebugger(),
                                UUID.randomUUID(),
                                new EventHost<>(),
                                new EventHost<>()
                        )
                ),
                arguments
        );
    }
}
