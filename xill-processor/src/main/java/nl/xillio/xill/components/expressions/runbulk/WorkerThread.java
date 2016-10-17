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

import me.biesaart.utils.Log;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.StoppableDebugger;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Robot;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.errors.XillParsingException;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Thread executing the {@link RunBulkExpression} jobs
 */
class WorkerThread extends Thread {
    private static final Logger LOGGER = Log.get();

    private final BlockingQueue<MetaExpression> queue;
    private final RunBulkControl control;

    private boolean stopOnError = false;

    private final WorkerRobotFactory robotFactory;

    /**
     * Create a worker
     * @param queue Queue for receiving jobs from master
     * @param control Controls runBulk threads
     * @param stopOnError Whether to stop the thread when an error occurs
     * @param robotFactory The factory compiling robots
     */
    public WorkerThread(final BlockingQueue<MetaExpression> queue, final RunBulkControl control,
                 boolean stopOnError, WorkerRobotFactory robotFactory) {
        super("RunBulk WorkerThread");
        this.queue = queue;
        this.control = control;

        this.stopOnError = stopOnError;

        this.robotFactory = robotFactory;
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
        } catch (WorkerCompileException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        }  catch (Exception e) {
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
    private boolean runRobot(Debugger debugger, File calledRobotFile, MetaExpression arg) throws WorkerCompileException {
        StoppableDebugger childDebugger = (StoppableDebugger) debugger.createChild();
        childDebugger.setStopOnError(stopOnError);

        Robot robot = robotFactory.construct(calledRobotFile, childDebugger);

        try {
            robot.setArgument(arg);

            robot.process(childDebugger);
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
