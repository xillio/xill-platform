package nl.xillio.xill.webservice.xill;

import me.biesaart.utils.FileUtils;
import nl.xillio.xill.webservice.XillRuntimeConfiguration;
import nl.xillio.xill.webservice.exceptions.XillCompileException;
import nl.xillio.xill.webservice.model.XillRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
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

    /**
     * Deploy a robot to be run during tests to a temporary directory.
     *
     * @throws IOException When deploying fails
     */
    @BeforeTest
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
    @AfterTest
    public void removeRobot() throws IOException {
       FileUtils.forceDelete(workingDirectory.toFile());
    }

    /**
     * Test running a single robot returning a result.
     * @param xillRuntime The runtime, will be injected
     * @throws XillCompileException When compilation fails
     */
    @Test
    @DirtiesContext
    @Autowired
    public void testRunRobot(XillRuntime xillRuntime) throws XillCompileException {
        xillRuntime.compile(workingDirectory, robotPath);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input", 42);

        Object result = xillRuntime.runRobot(parameters);

        assertEquals(result, 42, "Value returned by the robot did not match expected");
    }

    /**
     * Test running a single robot multiple times with different parameters
     * @param xillRuntime The runtime, will be injected
     * @throws XillCompileException When compilation fails
     */
    @Test
    @DirtiesContext
    @Autowired
    public void testRunRobotMultiple(XillRuntime xillRuntime) throws XillCompileException {
        xillRuntime.compile(workingDirectory, robotPath);

        for (int i=0; i<4; i++) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("input", i);
            Object result = xillRuntime.runRobot(parameters);

            assertEquals(result, i, "Value returned by the robot did not match expected");
        }
    }
}
