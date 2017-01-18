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
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;


public class PropertyServiceTest extends TestUtils {
    @Test
    public void testGetPropertyFromProjectOverride() throws Exception {
        Path projectPath = Paths.get("project");
        RobotID robotID = RobotID.getInstance(projectPath.resolve("robot.xill").toFile(), projectPath.toFile());

        // Mock the file system
        FileSystemAccess fileSystemAccess = new FileSystemAccess() {
            @Override
            public boolean exists(Path file) {
                return file.equals(projectPath.resolve("xill.properties"));
            }

            @Override
            public InputStream read(Path file) throws IOException {
                return IOUtils.toInputStream("testProperty=Hello World");
            }
        };

        PropertyService propertyService = new PropertyService(new Properties(), fileSystemAccess, new ContextPropertiesResolver());

        String result = propertyService.getProperty("testProperty", null, new ConstructContext(
                robotID,
                robotID,
                null,
                null,
                null,
                null,
                null
        ));

        assertEquals(result, "Hello World");
    }

    @Test
    public void testGetPropertyFromProjectOverrideWithSpecialCharacters() throws Exception {
        Path projectPath = Paths.get("project");
        RobotID robotID = RobotID.getInstance(projectPath.resolve("robot.xill").toFile(), projectPath.toFile());

        // Mock the file system
        FileSystemAccess fileSystemAccess = new FileSystemAccess() {
            @Override
            public boolean exists(Path file) {
                return file.equals(projectPath.resolve("xill.properties"));
            }

            @Override
            public InputStream read(Path file) throws IOException {
                return IOUtils.toInputStream("testProperty=¿Héllø Wœrld?");
            }
        };

        PropertyService propertyService = new PropertyService(new Properties(), fileSystemAccess, new ContextPropertiesResolver());

        String result = propertyService.getProperty("testProperty", null, new ConstructContext(
                robotID,
                robotID,
                null,
                null,
                null,
                null,
                null
        ));

        assertEquals(result, "¿Héllø Wœrld?");
    }

    @Test
    public void testGetPropertyFromRobotOutsideOfProject() {
        Path projectPath = Paths.get("project");
        RobotID robotID = RobotID.getInstance(projectPath.resolve("../robot.xill").toFile(), projectPath.toFile());

        // Mock the file system
        FileSystemAccess fileSystemAccess = new FileSystemAccess() {
            @Override
            public boolean exists(Path file) {
                return file.equals(projectPath.resolve("defaults.properties"));
            }

            @Override
            public InputStream read(Path file) throws IOException {
                return IOUtils.toInputStream("testProperty=Hello World");
            }
        };

        PropertyService propertyService = new PropertyService(new Properties(), fileSystemAccess, new ContextPropertiesResolver());


        String result = propertyService.getProperty("testProperty", null, new ConstructContext(
                robotID,
                robotID,
                null,
                null,
                null,
                null,
                null
        ));

        assertNull(result);
    }

    @Test
    public void testGetPropertyDoesNotFailOnIOException() {
        Path projectPath = Paths.get("project");
        RobotID robotID = RobotID.getInstance(projectPath.resolve("../robot.xill").toFile(), projectPath.toFile());

        // Mock the file system
        FileSystemAccess fileSystemAccess = new FileSystemAccess() {
            @Override
            public boolean exists(Path file) {
                return true;
            }

            @Override
            public InputStream read(Path file) throws IOException {
                throw new IOException("UNIT TEST");
            }
        };

        PropertyService propertyService = new PropertyService(new Properties(), fileSystemAccess, new ContextPropertiesResolver());


        propertyService.getProperty("testProperty", null, new ConstructContext(
                robotID,
                robotID,
                null,
                null,
                null,
                null,
                null
        ));
    }

    @Test
    public void testPropertySubstitution() {
        Properties properties = new Properties();
        properties.put("name", "World");
        properties.put("greet", "Hello ${name}!");

        String value = new PropertyService(properties, mock(FileSystemAccess.class), new ContextPropertiesResolver()).getFormattedProperty("greet", null, new ConstructContext(
                RobotID.getInstance(new File("test/value.xill"), new File("test")),
                RobotID.dummyRobot(),
                null,
                null,
                null,
                null,
                null
        ));

        assertEquals(value, "Hello World!");
    }

    @Test
    public void testEscapedPropertySubstitution() {
        Properties properties = new Properties();
        properties.put("name", "World");
        properties.put("greet", "Hello $${name}!");

        String value = new PropertyService(properties, mock(FileSystemAccess.class), new ContextPropertiesResolver()).getFormattedProperty("greet", null, new ConstructContext(
                RobotID.getInstance(new File("test/value.xill"), new File("test")),
                RobotID.dummyRobot(),
                null,
                null,
                null,
                null,
                null
        ));

        assertEquals(value, "Hello $${name}!");
    }
}
