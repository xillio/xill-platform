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

import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.ConstructContext;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.*;


public class ContextPropertiesResolverTest {
    @Test
    public void testDefaultXillResolver() throws Exception {
        Path wdPath = Paths.get("this/is/a/project");
        ContextPropertiesResolver resolver = ContextPropertiesResolver.defaultXillResolver();
        ConstructContext context = new ConstructContext(
                wdPath,
                RobotID.build("file://project", "my/Robot.xill"),
                RobotID.dummyRobot(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        String workingDirectory = resolver.resolve("xill.projectPath", context).get();
        String robotUrl = resolver.resolve("xill.robotUrl", context).get();

        assertTrue(wdPath.endsWith(workingDirectory));
        assertTrue(robotUrl.contains("project/my/Robot.xill"));
    }

    @Test
    public void testResolve() throws Exception {
        ContextPropertiesResolver resolver = new ContextPropertiesResolver();
        resolver.register("test", constructContext -> "Hello World");

        assertEquals(resolver.resolve("test", null).get(), "Hello World");
        assertFalse(resolver.resolve("nope", null).isPresent());
    }

}
