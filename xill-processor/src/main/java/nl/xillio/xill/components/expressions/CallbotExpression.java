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
package nl.xillio.xill.components.expressions;

import me.biesaart.utils.Log;
import nl.xillio.plugins.XillPlugin;
import nl.xillio.xill.XillProcessor;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.DefaultOutputHandler;
import nl.xillio.xill.api.LogUtil;
import nl.xillio.xill.api.OutputHandler;
import nl.xillio.xill.api.components.*;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.errors.XillParsingException;
import org.slf4j.Logger;
import xill.RobotLoader;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This class represents calling another robot
 */
public class CallbotExpression implements Processable {

    private static final Logger LOGGER = Log.get();
    private final Logger robotLogger;
    private Path workingDirectory;
    private final Processable path;
    private final RobotID robotID;
    private final List<XillPlugin> plugins;
    private Processable argument;
    private final OutputHandler outputHandler;
    private final RobotLoader loader;

    /**
     * Create a new {@link CallbotExpression}
     *
     * @param workingDirectory  the working directory
     * @param path              the path of the called bot
     * @param robotID           the root robot of this tree
     * @param plugins           the current plugin loader
     * @param outputHandler     the event handler for all output
     * @param loader
     */
    public CallbotExpression(final Path workingDirectory, final Processable path, final RobotID robotID, final List<XillPlugin> plugins, OutputHandler outputHandler, RobotLoader loader) {
        this.workingDirectory = workingDirectory;
        this.path = path;
        this.robotID = robotID;
        this.plugins = plugins;
        robotLogger = LogUtil.getLogger(robotID, new DefaultOutputHandler());
        this.outputHandler = outputHandler;
        this.loader = loader;
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) {
        String otherRobot = path.process(debugger).get().getStringValue();

        LOGGER.debug("Evaluating callbot for " + otherRobot);
        URL otherRobotURL = loader.getRobot(otherRobot);

        if (otherRobotURL == null) {
            throw new RobotRuntimeException("Called robot " + otherRobot + " does not exist.");
        }

        // Process the robot
        try {
            Debugger childDebugger = debugger.createChild();
            XillProcessor processor = new XillProcessor(workingDirectory, otherRobot, loader, plugins, childDebugger);
            processor.setOutputHandler(outputHandler);
            processor.compileAsSubRobot(robotID);

            try {
                nl.xillio.xill.api.components.Robot robot = processor.getRobot();

                if (argument != null) {
                    InstructionFlow<MetaExpression> argumentResult = argument.process(debugger);

                    robot.setArgument(argumentResult.get());
                }

                InstructionFlow<MetaExpression> result = processor.getRobot().process(childDebugger);

                if (result.hasValue()) {
                    return InstructionFlow.doResume(result.get());
                }
            } catch (Exception e) {
                if (e instanceof RobotRuntimeException) {
                    int line = childDebugger.getStackTrace().get(childDebugger.getStackDepth()).getLineNumber();
                    childDebugger.endInstruction(null, null); //pop from stack so stacktrace can be made.
                    throw new RobotRuntimeException("Caused by '" + otherRobot + "' (line " + line + ")", e);
                }
                throw new RobotRuntimeException("An exception occurred while evaluating " + otherRobot, e);
            } finally {
                debugger.removeChild(childDebugger);
            }

        } catch (IOException e) {
            throw new RobotRuntimeException("Error while calling robot: " + e.getMessage(), e);
        } catch (XillParsingException e) {
            throw new RobotRuntimeException("Error while parsing robot: " + e.getMessage(), e);
        } catch (Exception e) {
            debugger.handle(e);
        }

        return InstructionFlow.doResume(ExpressionBuilderHelper.NULL);
    }

    @Override
    public Collection<Processable> getChildren() {
        return Collections.singletonList(path);
    }

    /**
     * @return the robotLogger
     */
    public Logger getRobotLogger() {
        return robotLogger;
    }

    /**
     * Set the argument that will be passed to the called robot
     *
     * @param argument the argument to be passed to the called robot.
     */
    public void setArgument(final Processable argument) {
        this.argument = argument;
    }

}
