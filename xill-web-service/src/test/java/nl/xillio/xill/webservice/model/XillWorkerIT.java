package nl.xillio.xill.webservice.model;

import me.biesaart.utils.Log;
import nl.xillio.xill.webservice.RobotDeployer;
import nl.xillio.xill.webservice.XillRuntimeConfiguration;
import nl.xillio.xill.webservice.exceptions.XillCompileException;
import nl.xillio.xill.webservice.exceptions.XillInvalidStateException;
import nl.xillio.xill.webservice.xill.Log4JOutputHandler;
import nl.xillio.xill.webservice.xill.XillEnvironmentFactory;
import nl.xillio.xill.webservice.xill.XillRuntimeImpl;
import nl.xillio.xill.webservice.xill.XillRuntimeProperties;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.inject.Provider;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * @author Geert Konijnendijk
 */
@ContextConfiguration(classes = { XillRuntimeConfiguration.class, XillRuntimeImpl.class,
        XillEnvironmentFactory.class, XillRuntimeProperties.class, Log4JOutputHandler.class})
public class XillWorkerIT extends AbstractTestNGSpringContextTests {

    private RobotDeployer deployer;

    @Autowired
    private Provider<XillRuntime> runtimeProvider;

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
    public void testMultipleWorkers() throws XillCompileException, XillInvalidStateException {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("input", 42);

        XillWorker worker = new XillWorker(runtimeProvider.get(), deployer.getWorkingDirectory(), RobotDeployer.RETURN_ROBOT);
        Object result = worker.run(arguments);

        assertEquals(result, 42);
    }

    @Test
    public void testAbort() throws Exception {
        XillWorker worker = new XillWorker(runtimeProvider.get(), deployer.getWorkingDirectory(), RobotDeployer.WAIT_ROBOT);

        Thread runThread = new Thread(() -> {
            try {
                worker.run(MapUtils.EMPTY_MAP);
            } catch (XillInvalidStateException e) {
                throw new AssertionError(e);
            }
        });

        runThread.start();

        while (true) {
            try {
                worker.abort();
            } catch (XillInvalidStateException e) {
                continue;
            }
            break;
        }

        assertEquals(worker.getState(), XillWorkerState.ABORTING);

        runThread.join();

        assertEquals(worker.getState(), XillWorkerState.IDLE);
    }

}
