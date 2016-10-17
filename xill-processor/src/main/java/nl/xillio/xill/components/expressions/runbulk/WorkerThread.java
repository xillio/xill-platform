package nl.xillio.xill.components.expressions.runbulk;

import me.biesaart.utils.Log;
import nl.xillio.plugins.XillPlugin;
import nl.xillio.xill.XillProcessor;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.OutputHandler;
import nl.xillio.xill.api.StoppableDebugger;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Robot;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.errors.XillParsingException;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Thread executing the {@link RunBulkExpression} jobs
 */
class WorkerThread extends Thread {
    private static final Logger LOGGER = Log.get();

    private final BlockingQueue<MetaExpression> queue;
    private final RunBulkControl control;

    private final List<XillPlugin> plugins;
    private boolean stopOnError = false;
    private final OutputHandler outputHandler;

    private final RobotID robotID;

    /**
     * Create a worker
     * @param queue Queue for receiving jobs from master
     * @param control Controls runBulk threads
     * @param plugins The loaded plugins
     * @param stopOnError Whether to stop the thread when an error occurs
     * @param outputHandler Handler to log to
     * @param robotID The ID of the robot to run
     */
    public WorkerThread(final BlockingQueue<MetaExpression> queue, final RunBulkControl control, List<XillPlugin> plugins,
                 boolean stopOnError, OutputHandler outputHandler, RobotID robotID) {
        super("RunBulk WorkerThread");
        this.queue = queue;
        this.control = control;

        this.plugins = plugins;
        this.stopOnError = stopOnError;
        this.outputHandler = outputHandler;

        this.robotID = robotID;
    }

    /**
     * Start processing jobs received from the master
     */
    @Override
    public void run() {
        try {
            while (!control.shouldStop()) {
                processQueueItem(queue.poll(100, TimeUnit.MILLISECONDS));
            }
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while processing queue item", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Run the robot for one queue item
     * @param item The item to process
     */
    private void processQueueItem(final MetaExpression item) {
        if (item != null) {
            if (!processRobot(control.getDebugger(), control.getCalledRobotFile(), item)) {
                control.signalStop();
            } else {
                if (control.getDebugger().shouldStop()) {
                    control.signalStop();
                } else {
                    control.incRunCount();
                }
            }
        }
    }

    /**
     * @return true if the robot ended up successfully, false if there was an error or interruption, etc.
     */
    private boolean processRobot(final Debugger debugger, final File calledRobotFile, final MetaExpression arg) {
        // Process the robot
        try {
            return runRobot(debugger, calledRobotFile, arg);
        } catch (IOException e) {
            throw new RobotRuntimeException("Error while calling robot: " + e.getMessage(), e);
        } catch (XillParsingException e) {
            throw new RobotRuntimeException("Error while parsing robot: " + e.getMessage(), e);
        } catch (Exception e) {
            debugger.handle(e);
        }

        return false; // Something went wrong
    }

    /**
     * Run a single robot
     * @param debugger The debugger to use as parent debugger
     * @param calledRobotFile The file to process
     * @param arg The argument input to the robot
     * @return True if the robot was successful, false otherwise
     * @throws IOException When reading the robot fails
     * @throws XillParsingException When a compile error occurs
     */
    private boolean runRobot(Debugger debugger, File calledRobotFile, MetaExpression arg) throws IOException, XillParsingException {
        StoppableDebugger childDebugger = (StoppableDebugger) debugger.createChild();
        childDebugger.setStopOnError(stopOnError);

        XillProcessor processor = new XillProcessor(robotID.getProjectPath(), calledRobotFile, plugins, childDebugger);
        processor.setOutputHandler(outputHandler);
        processor.compileAsSubRobot(robotID);

        try {
            Robot robot = processor.getRobot();
            robot.setArgument(arg);

            processor.getRobot().process(childDebugger);
            // Ignoring the returned value from the bot as it won't be processed anyway

            return !(stopOnError && childDebugger.hasErrorOccurred());

        } catch (RobotRuntimeException e) {
            int line = childDebugger.getStackTrace().get(childDebugger.getStackDepth()).getLineNumber();
            childDebugger.endInstruction(null, null);
            throw new RobotRuntimeException("Caused by '" + calledRobotFile.getName() + "' (line " + line + ")", e);
        } catch (Exception e) {
            throw new RobotRuntimeException("An exception occurred while evaluating " + calledRobotFile.getAbsolutePath(), e);
        } finally {
            debugger.removeChild(childDebugger);
        }
    }
}
