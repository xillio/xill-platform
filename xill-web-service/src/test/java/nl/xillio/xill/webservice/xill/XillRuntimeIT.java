package nl.xillio.xill.webservice.xill;

import me.biesaart.utils.FileUtils;
import nl.xillio.xill.api.OutputHandler;
import nl.xillio.xill.webservice.XillRuntimeConfiguration;
import nl.xillio.xill.webservice.exceptions.XillCompileException;
import nl.xillio.xill.webservice.model.XillRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
     *
     * @param xillRuntime
     * @throws XillCompileException
     */
    @Test
    @Autowired
    public void testRunRobot(XillRuntime xillRuntime) throws XillCompileException {
        xillRuntime.compile(workingDirectory, robotPath);
        Object result = xillRuntime.runRobot(null);

        assertEquals(result, 42);
    }

}
