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
package nl.xillio.xill.webservice.model;

import nl.xillio.xill.webservice.RobotDeployer;
import nl.xillio.xill.webservice.RuntimeConfiguration;
import nl.xillio.xill.webservice.exceptions.BaseException;
import nl.xillio.xill.webservice.exceptions.InvalidStateException;
import nl.xillio.xill.webservice.xill.*;
import org.apache.commons.collections.MapUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Geert Konijnendijk
 */
@ContextConfiguration(classes = { RuntimeConfiguration.class, RuntimeImpl.class,
        EnvironmentFactory.class, RuntimeProperties.class, Log4JOutputHandler.class,
        RuntimePooledObjectFactory.class, WorkerFactory.class})
public class WorkerIT extends AbstractTestNGSpringContextTests {

    private RobotDeployer deployer;

    @Inject
    private WorkerFactory workerFactory;

    /**
     * Deploy a robot to be run during tests to a temporary directory.
     *
     * @throws IOException When deploying fails
     */
    @BeforeClass
    public void deployRobot() throws IOException {
        deployer = new RobotDeployer();
        deployer.deployRobots();
    }


    /**
     * Delete the temporary directory containing the robot.
     *
     * @throws IOException When deleting fails
     */
    @AfterClass
    public void removeRobot() throws IOException {
        deployer.removeRobots();
    }

    /**
     * Test creating multiple workers in multiple threads and running a robot.
     */
    @Test(threadPoolSize = 2, invocationCount = 4)
    public void testMultipleWorkers() throws Exception {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("input", 42);

        Worker worker = workerFactory.constructWorker(deployer.getWorkingDirectory(), RobotDeployer.RETURN_ROBOT_NAME);
        Object result = worker.run(arguments);
        worker.close();

        assertEquals(result, 42);
    }

    @Test
    public void testAbort() throws BaseException, InterruptedException {
        Worker worker = workerFactory.constructWorker(deployer.getWorkingDirectory(), RobotDeployer.WAIT_ROBOT_NAME);

        // Start a robot in a new thread
        Thread runThread = new Thread(() -> {
            try {
                worker.run(MapUtils.EMPTY_MAP);
            } catch (InvalidStateException e) {
                throw new AssertionError(e);
            }
        });
        runThread.start();

        await().atMost(5, SECONDS).until(worker::getState, equalTo(WorkerState.RUNNING));

        // Abort the running robot in a new thread
        Thread abortThread = new Thread(() -> {
            try {
                worker.abort();
            } catch (InvalidStateException e) {
                throw new AssertionError(e);
            }
        });
        abortThread.start();


        await().atMost(5, SECONDS).until(worker::getState, equalTo(WorkerState.ABORTING));

        abortThread.join();
        runThread.join();

        await().atMost(5, SECONDS).until(() -> !abortThread.isAlive() && !runThread.isAlive());

        assertEquals(worker.getState(), WorkerState.IDLE);
    }

}
