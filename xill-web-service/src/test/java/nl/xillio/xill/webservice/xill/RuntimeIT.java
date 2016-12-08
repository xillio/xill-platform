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
import nl.xillio.xill.webservice.RuntimeConfiguration;
import nl.xillio.xill.webservice.exceptions.BaseException;
import nl.xillio.xill.webservice.exceptions.CompileException;
import nl.xillio.xill.webservice.exceptions.RobotNotFoundException;
import nl.xillio.xill.webservice.model.Runtime;
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
 * Integration tests for the {@link RuntimeImpl} and pooling.
 *
 * @author Geert Konijnendijk
 */
@ContextConfiguration(classes = { RuntimeConfiguration.class, RuntimeImpl.class,
        EnvironmentFactory.class, RuntimeProperties.class, Log4JOutputHandler.class,
        RuntimePooledObjectFactory.class})
public class RuntimeIT extends AbstractTestNGSpringContextTests {

    private RobotDeployer deployer;

    @Autowired
    private ObjectPool<Runtime> runtimePool;

    private Runtime runtime;

    /**
     * Deploy a robot to be run during tests to a temporary directory.
     *
     * @throws IOException When deploying fails
     */
    @BeforeClass
    public void deployRobot() throws Exception {
        deployer = new RobotDeployer();
        deployer.deployRobots();
        runtime = runtimePool.borrowObject();
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
     * @throws CompileException When compilation fails
     */
    @Test
    public void testRunRobot() throws BaseException {
        runtime.compile(deployer.getWorkingDirectory(), RETURN_ROBOT_NAME);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input", 42);

        Object result = runtime.runRobot(parameters);

        assertEquals(result, 42, "Value returned by the robot did not match expected");
    }

    /**
     * Test running a single robot multiple times with different parameters.
     *
     * @throws CompileException When compilation fails
     */
    @Test
    public void testRunRobotMultiple() throws BaseException {
        runtime.compile(deployer.getWorkingDirectory(), RETURN_ROBOT_NAME);

        for (int i=0; i<4; i++) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("input", i);
            Object result = runtime.runRobot(parameters);

            assertEquals(result, i, "Value returned by the robot did not match expected");
        }
    }
}
