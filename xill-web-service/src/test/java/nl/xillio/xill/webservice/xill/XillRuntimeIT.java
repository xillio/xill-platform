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

import nl.xillio.xill.webservice.RobotDeployer;
import nl.xillio.xill.webservice.XillRuntimeConfiguration;
import nl.xillio.xill.webservice.exceptions.XillCompileException;
import nl.xillio.xill.webservice.exceptions.XillNotFoundException;
import nl.xillio.xill.webservice.model.XillRuntime;
import org.apache.commons.pool2.ObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static nl.xillio.xill.webservice.RobotDeployer.RETURN_ROBOT_NAME;
import static org.testng.Assert.assertEquals;

/**
 * Integration tests for the {@link XillRuntimeImpl} and pooling.
 *
 * @author Geert Konijnendijk
 */
@ContextConfiguration(classes = { XillRuntimeConfiguration.class, XillRuntimeImpl.class,
        XillEnvironmentFactory.class, XillRuntimeProperties.class, Log4JOutputHandler.class,
        RuntimePooledObjectFactory.class})
public class XillRuntimeIT extends AbstractTestNGSpringContextTests {

    private RobotDeployer deployer;

    @Autowired
    private ObjectPool<XillRuntime> runtimePool;

    private XillRuntime xillRuntime;

    /**
     * Deploy a robot to be run during tests to a temporary directory.
     *
     * @throws IOException When deploying fails
     */
    @BeforeClass
    public void deployRobot() throws Exception {
        deployer = new RobotDeployer();
        deployer.deployRobots();
        xillRuntime = runtimePool.borrowObject();
    }

    /**
     * Delete the temporary directory containing the robot
     *
     * @throws IOException When deleting fails
     */
    @AfterClass
    public void removeRobot() throws IOException {
       deployer.removeRobots();
    }

    /**
     * Test running a single robot returning a result.
     *
     * @throws XillCompileException When compilation fails
     */
    @Test
    public void testRunRobot() throws XillCompileException, ExecutionException, XillNotFoundException {
        xillRuntime.compile(deployer.getWorkingDirectory(), RETURN_ROBOT_NAME);

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
    public void testRunRobotMultiple() throws XillCompileException, ExecutionException, XillNotFoundException {
        xillRuntime.compile(deployer.getWorkingDirectory(), RETURN_ROBOT_NAME);

        for (int i=0; i<4; i++) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("input", i);
            Object result = xillRuntime.runRobot(parameters);

            assertEquals(result, i, "Value returned by the robot did not match expected");
        }
    }
}
