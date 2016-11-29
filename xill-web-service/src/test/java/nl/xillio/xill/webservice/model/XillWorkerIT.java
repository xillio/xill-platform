package nl.xillio.xill.webservice.model;

import nl.xillio.xill.webservice.RobotDeployer;
import nl.xillio.xill.webservice.XillRuntimeConfiguration;
import nl.xillio.xill.webservice.exceptions.XillBaseException;
import nl.xillio.xill.webservice.exceptions.XillInvalidStateException;
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Geert Konijnendijk
 */
@ContextConfiguration(classes = { XillRuntimeConfiguration.class, XillRuntimeImpl.class,
        XillEnvironmentFactory.class, XillRuntimeProperties.class, Log4JOutputHandler.class,
        RuntimePooledObjectFactory.class, XillWorkerFactory.class})
public class XillWorkerIT extends AbstractTestNGSpringContextTests {

    private RobotDeployer deployer;

    @Inject
    private XillWorkerFactory xillWorkerFactory;

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
     * Delete the temporary directory containing the robot
     *
     * @throws IOException When deleting fails
     */
    @AfterClass
    public void removeRobot() throws IOException {
        deployer.removeRobots();
    }

    /**
     * Test creating multiple workers in multiple threads and running a robot
     */
    @Test(threadPoolSize = 2, invocationCount = 4)
    public void testMultipleWorkers() throws Exception {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("input", 42);

        XillWorker worker = xillWorkerFactory.constructWorker(deployer.getWorkingDirectory(), RobotDeployer.RETURN_ROBOT_NAME);
        Object result = worker.run(arguments);
        worker.close();

        assertEquals(result, 42);
    }

    @Test
    public void testAbort() throws XillBaseException, InterruptedException {
        XillWorker worker = xillWorkerFactory.constructWorker(deployer.getWorkingDirectory(), RobotDeployer.WAIT_ROBOT_NAME);

        // Start a robot in a new thread
        Thread runThread = new Thread(() -> {
            try {
                worker.run(MapUtils.EMPTY_MAP);
            } catch (XillInvalidStateException e) {
                throw new AssertionError(e);
            }
        });
        runThread.start();

        // Wait until the robot is running
        while (worker.getState() != XillWorkerState.RUNNING) {
            Thread.sleep(10);
            // If the robot finished running, something is wrong
            assertTrue(runThread.isAlive(), "The robot finished running before being aborted");
        }

        // Abort the running robot in a new thread
        Thread abortThread = new Thread(() -> {
            try {
                worker.abort();
            } catch (XillInvalidStateException e) {
                throw new AssertionError(e);
            }
        });
        abortThread.start();

        // Wait until the robot is aborting
        while (worker.getState() != XillWorkerState.ABORTING) {
            Thread.sleep(10);
            // When aborting finished, something is wrong
            assertTrue(abortThread.isAlive(), "Aborting finished without the state being changed");
        }

        abortThread.join();
        runThread.join();

        assertEquals(worker.getState(), XillWorkerState.IDLE);
    }

}
