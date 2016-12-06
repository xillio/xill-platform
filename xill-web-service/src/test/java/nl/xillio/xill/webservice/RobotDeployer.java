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
package nl.xillio.xill.webservice;

import me.biesaart.utils.FileUtils;

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

    public static final String RETURN_ROBOT_NAME = "return";
    public static final String WAIT_ROBOT_NAME = "wait";
    public static final String RETURN_ROBOT = RETURN_ROBOT_NAME + ".xill";
    public static final String WAIT_ROBOT = WAIT_ROBOT_NAME + ".xill";

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
