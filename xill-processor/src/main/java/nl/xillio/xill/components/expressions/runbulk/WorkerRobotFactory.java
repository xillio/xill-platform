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
package nl.xillio.xill.components.expressions.runbulk;

import nl.xillio.plugins.XillPlugin;
import nl.xillio.xill.XillProcessor;
import nl.xillio.xill.api.OutputHandler;
import nl.xillio.xill.api.StoppableDebugger;
import nl.xillio.xill.api.components.Robot;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.errors.XillParsingException;
import nl.xillio.xill.loaders.AbstractRobotLoader;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

/**
 * A factory creating {@link nl.xillio.xill.api.components.Robot robots} for {@link WorkerThread}.
 */
class WorkerRobotFactory {

    private Path workingDirectory;
    private RobotID robotID;
    private List<XillPlugin> plugins;
    private OutputHandler outputHandler;

    public WorkerRobotFactory(final Path workingDirectory, RobotID robotID, List<XillPlugin> plugins, OutputHandler outputHandler) {
        this.workingDirectory = workingDirectory;
        this.robotID = robotID;
        this.plugins = plugins;
        this.outputHandler = outputHandler;
    }

    /**
     * Compile a {@link Robot}.
     *
     * @param robotPath     The path to the robot to compile
     * @param loader        The robotLoader that is used
     * @param childDebugger The debugger to compile with
     * @return The compiled robot
     * @throws IOException          When the robot could not be read
     * @throws XillParsingException When the robot could not be compiled
     */
    public Robot construct(String robotPath, AbstractRobotLoader loader, StoppableDebugger childDebugger) throws WorkerCompileException {
        XillProcessor processor = null;
        try {
            URL robotResource = loader.getResource(robotPath);
            if (robotResource == null) {
                throw new WorkerCompileException("Could not find robot: " + robotPath);
            }
            processor = new XillProcessor(workingDirectory, new RobotID(robotResource, robotPath), loader, plugins, childDebugger);
            processor.setOutputHandler(outputHandler);
            processor.compileAsSubRobot(robotID);
        } catch (XillParsingException e) {
            throw new WorkerCompileException("Could not parse robot", e);
        }
        return processor.getRobot();
    }
}
