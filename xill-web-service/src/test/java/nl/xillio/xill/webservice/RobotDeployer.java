package nl.xillio.xill.webservice;

import me.biesaart.utils.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper class for deploying and removing testing robots
 *
 * @author Geert Konijnendijk
 */
public class RobotDeployer {

    public static final String RETURN_ROBOT = "return.xill";
    public static final String WAIT_ROBOT = "wait.xill";

    Path workingDirectory;

    /**
     * Deploy a robot to be run during tests to a temporary directory.
     *
     * @throws IOException When deploying fails
     */
    public void deployRobots() throws IOException {
        workingDirectory = Files.createTempDirectory("xillRuntimeIT");
        Path returnRobotPath = Paths.get(RETURN_ROBOT);
        FileUtils.copyInputStreamToFile(ClassLoader.getSystemResourceAsStream("xill/" + RETURN_ROBOT), workingDirectory.resolve(returnRobotPath).toFile());
        Path waitRobotPath = Paths.get(WAIT_ROBOT);
        FileUtils.copyInputStreamToFile(ClassLoader.getSystemResourceAsStream("xill/" + WAIT_ROBOT), workingDirectory.resolve(waitRobotPath).toFile());
    }

    /**
     * Delete the temporary directory containing the robot
     *
     * @throws IOException When deleting fails
     */
    public void removeRobots() throws IOException {
        FileUtils.forceDelete(workingDirectory.toFile());
    }

    /**
     * @return The path the robots are deployed in
     */
    public Path getWorkingDirectory() {
        return workingDirectory;
    }
}
