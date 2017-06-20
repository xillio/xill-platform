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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.io.ResourceLoader;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;


public class PropertyServiceTest extends TestUtils {
    @Test
    public void testGetPropertyFromProjectOverride() throws Exception {
        Path projectPath = Paths.get("project");
        RobotID robotID = RobotID.dummyRobot();

        // Mock the ResourceLoader
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getResourceAsStream(anyString())).thenReturn(IOUtils.toInputStream("testProperty=Hello World"));

        PropertyService propertyService = new PropertyService(new Properties(), new ContextPropertiesResolver());

        String result = propertyService.getProperty("testProperty", null,
                new ConstructContext(
                        projectPath,
                        robotID,
                        robotID,
                        null,
                        null,
                        null,
                        null,
                        null,
                        resourceLoader));

        assertEquals(result, "Hello World");
    }

    @Test
    public void testGetPropertyFromProjectOverrideWithSpecialCharacters() throws Exception {
        Path projectPath = Paths.get("project");
        RobotID robotID = RobotID.dummyRobot();

        // Mock the RobotLoader
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getResourceAsStream(anyString())).thenReturn(IOUtils.toInputStream("testProperty=¿Héllø Wœrld?"));

        PropertyService propertyService = new PropertyService(new Properties(), new ContextPropertiesResolver());

        String result = propertyService.getProperty("testProperty", null, new ConstructContext(
                projectPath,
                robotID,
                robotID,
                null,
                null,
                null,
                null,
                null,
                resourceLoader));

        assertEquals(result, "¿Héllø Wœrld?");
    }

    @Test
    public void testGetPropertyDoesNotFailOnIOException() throws Exception {
        Path projectPath = Paths.get("project");
        RobotID robotID = RobotID.dummyRobot();

        // Mock the RobotLoader
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getResourceAsStream(anyString())).thenThrow(new IOException("UNIT TEST"));


        PropertyService propertyService = new PropertyService(new Properties(), new ContextPropertiesResolver());


        propertyService.getProperty("testProperty", null, new ConstructContext(
                projectPath,
                robotID,
                robotID,
                null,
                null,
                null,
                null,
                null,
                resourceLoader));
    }

    @Test
    public void testPropertySubstitution() throws Exception {
        Properties properties = new Properties();
        properties.put("name", "World");
        properties.put("greet", "Hello ${name}!");

        String value = new PropertyService(properties, new ContextPropertiesResolver()).getFormattedProperty("greet", null, new ConstructContext(
                Paths.get("test"),
                RobotID.build("file://test", "value.xill"),
                RobotID.dummyRobot(),
                null,
                null,
                null,
                null,
                null,
                mock(ResourceLoader.class)));

        assertEquals(value, "Hello World!");
    }

    @Test
    public void testEscapedPropertySubstitution() throws Exception {
        Properties properties = new Properties();
        properties.put("name", "World");
        properties.put("greet", "Hello $${name}!");

        String value = new PropertyService(properties, new ContextPropertiesResolver()).getFormattedProperty("greet", null, new ConstructContext(
                Paths.get("test"),
                RobotID.build("file://test", "test.xill"),
                RobotID.dummyRobot(),
                null,
                null,
                null,
                null,
                null,
                mock(ResourceLoader.class)));

        assertEquals(value, "Hello $${name}!");
    }
}
