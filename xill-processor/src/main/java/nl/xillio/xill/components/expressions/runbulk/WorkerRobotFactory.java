package nl.xillio.xill.components.expressions.runbulk;

import nl.xillio.plugins.XillPlugin;
import nl.xillio.xill.XillProcessor;
import nl.xillio.xill.api.OutputHandler;
import nl.xillio.xill.api.StoppableDebugger;
import nl.xillio.xill.api.components.Robot;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.errors.XillParsingException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A factory creating {@link nl.xillio.xill.api.components.Robot robots} for {@link WorkerThread}.
 */
class WorkerRobotFactory {

    private RobotID robotID;
    private List<XillPlugin> plugins;
    private OutputHandler outputHandler;

    public WorkerRobotFactory(RobotID robotID, List<XillPlugin> plugins, OutputHandler outputHandler) {
        this.robotID = robotID;
        this.plugins = plugins;
        this.outputHandler = outputHandler;
    }

    /**
     * Compile a {@link Robot}
     * @param calledRobotFile The file to compile
     * @param childDebugger The debugger to compile with
     * @return The compiled robot
     * @throws IOException When the robot could not be read
     * @throws XillParsingException When the robot could not be compiled
     */
    public Robot construct(File calledRobotFile, StoppableDebugger childDebugger) throws WorkerCompileException {
        XillProcessor processor = null;
        try {
            processor = new XillProcessor(robotID.getProjectPath(), calledRobotFile, plugins, childDebugger);
            processor.setOutputHandler(outputHandler);
            processor.compileAsSubRobot(robotID);
        } catch (IOException e) {
            throw new WorkerCompileException("Could not read robot", e);
        } catch (XillParsingException e) {
            throw new WorkerCompileException("Could not parse robot", e);
        }
        return processor.getRobot();
    }
}
