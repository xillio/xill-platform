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
package nl.xillio.xill.webservice.xill;

import me.biesaart.utils.FileUtils;
import nl.xillio.xill.webservice.XillRuntimeConfiguration;
import nl.xillio.xill.webservice.exceptions.XillCompileException;
import nl.xillio.xill.webservice.model.XillRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Integration tests for the {@link XillRuntimeImpl} and pooling.
 *
 * @author Geert Konijnendijk
 */
@ContextConfiguration(classes = { XillRuntimeConfiguration.class, XillRuntimeImpl.class,
        XillEnvironmentFactory.class, XillRuntimeProperties.class, Log4JOutputHandler.class})
public class XillRuntimeIT extends AbstractTestNGSpringContextTests {

    private Path workingDirectory;
    private Path robotPath;

    @Autowired
    private XillRuntime xillRuntime;

    /**
     * Deploy a robot to be run during tests to a temporary directory.
     *
     * @throws IOException When deploying fails
     */
    @BeforeClass
    public void deployRobot() throws IOException {
        workingDirectory = Files.createTempDirectory("xillRuntimeIT");
        robotPath = Paths.get("return.xill");
        FileUtils.copyInputStreamToFile(ClassLoader.getSystemResourceAsStream("xill/return.xill"), workingDirectory.resolve(robotPath).toFile());
    }

    /**
     * Delete the temporary directory containing the robot
     *
     * @throws IOException When deleting fails
     */
    @AfterClass
    public void removeRobot() throws IOException {
       FileUtils.forceDelete(workingDirectory.toFile());
    }

    /**
     * Test running a single robot returning a result.
     *
     * @throws XillCompileException When compilation fails
     */
    @Test
    @DirtiesContext
    public void testRunRobot() throws XillCompileException {
        xillRuntime.compile(workingDirectory, robotPath);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input", 42);

        Object result = xillRuntime.runRobot(parameters);

        assertEquals(result, 42, "Value returned by the robot did not match expected");
    }

    /**
     * Test running a single robot multiple times with different parameters.
     *
     * @throws XillCompileException When compilation fails
     */
    @Test
    @DirtiesContext
    public void testRunRobotMultiple() throws XillCompileException {
        xillRuntime.compile(workingDirectory, robotPath);

        for (int i=0; i<4; i++) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("input", i);
            Object result = xillRuntime.runRobot(parameters);

            assertEquals(result, i, "Value returned by the robot did not match expected");
        }
    }
}
