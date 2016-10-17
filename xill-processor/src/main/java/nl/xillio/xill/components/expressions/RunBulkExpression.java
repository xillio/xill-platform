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
import nl.xillio.xill.Xill;
import nl.xillio.xill.XillProcessor;
import nl.xillio.xill.api.*;
import nl.xillio.xill.api.components.*;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.errors.XillParsingException;
import nl.xillio.xill.services.files.FileResolver;
import nl.xillio.xill.services.files.FileResolverImpl;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static nl.xillio.xill.api.components.ExpressionDataType.ATOMIC;
import static nl.xillio.xill.api.components.ExpressionDataType.LIST;

/**
 * This class represents calling another robot multiple times in a separate threads
 */
public class RunBulkExpression implements Processable {

    private static final Logger LOGGER = Log.get();
    private final Processable path;
    private final RobotID robotID;
    private final List<XillPlugin> plugins;
    private Processable argument;
    private final FileResolver resolver;
    private Processable options;
    private int maxThreadsVal = 0;
    private boolean stopOnError = false;
    private final OutputHandler outputHandler;

    private class Control {
        private int runCount = 0;
        private boolean stop = false;
        private final Debugger debugger;
        private final File calledRobotFile;

        public Control(final Debugger debugger, final File calledRobotFile) {
            this.debugger = debugger;
            this.calledRobotFile = calledRobotFile;
        }

        public Debugger getDebugger() {
            return debugger;
        }

        public File getCalledRobotFile() {
            return calledRobotFile;
        }

        public synchronized void incRunCount() {
            runCount++;
        }

        public int getRunCount() {
            return runCount;
        }

        public synchronized void signalStop() {
            stop = true;
        }

        public synchronized boolean shouldStop() {
            return stop;
        }
    }

    private class MasterThread extends Thread {
        private final Iterator<MetaExpression> source;
        private final BlockingQueue<MetaExpression> queue;
        private final Control control;

        public MasterThread(final Iterator<MetaExpression> source, final BlockingQueue<MetaExpression> queue, final Control control) {
            super("RunBulk MasterThread");
            this.source = source;
            this.queue = queue;
            this.control = control;
        }

        @Override
        public void run() {
            try {
                offerItemsToQueue();
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while waiting for queue item", e);
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Insert items into the queue while there are items and we should continue running.
         *
         * @throws InterruptedException When offering an item to the queue is interrupted
         */
        private void offerItemsToQueue() throws InterruptedException {
            while (source.hasNext() && !control.shouldStop()) {
                MetaExpression item = source.next();
                while (!queue.offer(item, 100, TimeUnit.MILLISECONDS)) {
                    if (control.shouldStop()) {
                        return;
                    }
                }
            }
        }
    }

    private class WorkerThread extends Thread {
        private final BlockingQueue<MetaExpression> queue;
        private final Control control;

        public WorkerThread(final BlockingQueue<MetaExpression> queue, final Control control) {
            super("RunBulk WorkerThread");
            this.queue = queue;
            this.control = control;
        }

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

    /**
     * Create a new {@link RunBulkExpression}
     *  @param path    the path of the called bot
     * @param robotID the root robot of this tree
     * @param plugins the current plugin loader
     * @param outputHandler
     */
    public RunBulkExpression(final Processable path, final RobotID robotID, final List<XillPlugin> plugins, OutputHandler outputHandler) {
        this.path = path;
        this.robotID = robotID;
        this.plugins = plugins;
        this.outputHandler = outputHandler;
        resolver = new FileResolverImpl();
        maxThreadsVal = 0;
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) {
        MetaExpression pathExpression = path.process(debugger).get();

        File otherRobot = resolver.buildPath(new ConstructContext(robotID, robotID, null, null, null, null, null), pathExpression).toFile();

        LOGGER.debug("Evaluating runBulk for " + otherRobot.getAbsolutePath());

        if (!otherRobot.exists()) {
            throw new RobotRuntimeException("Called robot " + otherRobot.getAbsolutePath() + " does not exist.");
        }

        if (!otherRobot.getName().endsWith(XillEnvironment.ROBOT_EXTENSION)) {
            throw new RobotRuntimeException("Can only call robots with the ." + XillEnvironment.ROBOT_EXTENSION + " extension.");
        }

        parseOptions();
        int robotRunCount = runBulk(debugger, otherRobot);

        return InstructionFlow.doResume(ExpressionBuilderHelper.fromValue(robotRunCount));
    }

    private Iterator<MetaExpression> getIterator(final MetaExpression result) {
        if (result.isNull()) {
            return null;
        }

        ExpressionDataType type = result.getType();
        if (type == ATOMIC) {
            if (!result.hasMeta(MetaExpressionIterator.class)) {
                List<MetaExpression> list = new LinkedList<>();
                list.add(result);
                return list.iterator();
            } else {
                return result.getMeta(MetaExpressionIterator.class);
            }
        } else if (type == LIST) { // Iterate over list
            List<MetaExpression> elements = result.getValue();
            return elements.iterator();
        } else {
            throw new RobotRuntimeException("Invalid argument!");
        }
    }

    /**
     * Run the called robot multiple times
     *
     * @return The number of robot runs
     */
    private int runBulk(final Debugger debugger, final File calledRobotFile) {
        // Evaluate argument
        if (argument == null) {
            return 0; // Nothing to do
        }

        if (maxThreadsVal == 0) {// Default value equals the number of logical cores
            maxThreadsVal = Runtime.getRuntime().availableProcessors();
        }

        // Get argument iterator
        InstructionFlow<MetaExpression> argumentResult = argument.process(debugger);
        Iterator<MetaExpression> source = getIterator(argumentResult.get());
        if (source == null) {
            return 0;
        }

        BlockingQueue<MetaExpression> queue = new ArrayBlockingQueue<>(maxThreadsVal);
        Control control = new Control(debugger, calledRobotFile);

        // Start master thread
        Thread master = new MasterThread(source, queue, control);
        master.start();

        // Start workers
        List<Thread> workingThreads = spawnWorkers(queue, control);

        // Wait for master to complete
        try {
            master.join();
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while waiting for join", e);
            Thread.currentThread().interrupt();
        }

        waitUntilDone(queue, control);

        control.signalStop();

        joinThreads(workingThreads);

        return control.getRunCount();
    }

    /**
     * Create {@link #maxThreadsVal} worker threads and start them
     * @param queue The queue to pass to the workers
     * @param control The control to pass to the workers
     * @return The worker threads
     */
    private List<Thread> spawnWorkers(BlockingQueue<MetaExpression> queue, Control control) {
        // Start working threads
        List<Thread> workingThreads = new LinkedList<>();
        for (int i = 0; i < maxThreadsVal; i++) {
            Thread worker = new WorkerThread(queue, control);
            worker.start();
            workingThreads.add(worker);
        }
        return workingThreads;
    }

    /**
     * Wait until either the queue is empty or we should stop due to some other reason signalled by {@link Control}
     * @param queue The queue to wait for to become empty
     * @param control Stop waiting when the control signals to stop
     */
    private void waitUntilDone(BlockingQueue<MetaExpression> queue, Control control) {
        // Wait for until entire queue is processed
        while (!control.shouldStop() && !queue.isEmpty()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while sleeping", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Join all threads and wait until they are joined
     * @param workingThreads The threads to join
     */
    private void joinThreads(List<Thread> workingThreads) {
        // Wait for all worker threads to complete
        workingThreads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while waiting for join", e);
                Thread.currentThread().interrupt();
            }
        });
    }

    @Override
    public Collection<Processable> getChildren() {
        return Collections.singletonList(path);
    }

    /**
     * Set the argument that will be used for running called robots.
     *
     * @param argument the value to which the argument needs to be set.
     */
    public void setArgument(final Processable argument) {
        this.argument = argument;
    }

    /**
     * Set the options that will be used when running called robots
     *
     * @param options the options to be set when running robots.
     */
    public void setOptions(final Processable options) {
        this.options = options;
    }

    /**
     * Parse the option expression
     */
    private void parseOptions() {
        if (options == null) {
            return;
        }
        MetaExpression optionVar = options.process(new NullDebugger()).get();
        if (optionVar.isNull()) {
            return;
        }

        if (optionVar.getType() != ExpressionDataType.OBJECT) {
            throw new RobotRuntimeException("Invalid max. threads value");
        }

        parseOptions(optionVar);
    }

    /**
     * Parse supported options from an OBJECT {@link MetaExpression}
     * @param optionVar The options OBJECT
     */
    private void parseOptions(MetaExpression optionVar) {
        Map<String, MetaExpression> optionParameters = optionVar.getValue();

        for (Map.Entry<String, MetaExpression> entry : optionParameters.entrySet()) {
            switch (entry.getKey()) {
                case "maxThreads":
                    parseMaxThreads(entry.getValue());
                    break;
                case "stopOnError":
                    parseStopOnError(entry.getValue());
                    break;
                default:
                    throw new RobotRuntimeException("Invalid option");
            }
        }
    }

    /**
     * Parse the stopOnError option from a {@link MetaExpression}
     * @param value The option value
     */
    private void parseStopOnError(MetaExpression value) {
        String stringValue = value.getStringValue();
        if ("yes".equals(stringValue)) {
            stopOnError = true;
        } else if ("no".equals(stringValue)) {
            stopOnError = false;
        } else {
            throw new RobotRuntimeException("Invalid onError value");
        }
    }

    /**
     * Parse the maxThreads option from a {@link MetaExpression}
     * @param value The option value
     */
    private void parseMaxThreads(MetaExpression value) {
        maxThreadsVal = value.getNumberValue().intValue();
        if (maxThreadsVal < 1) {
            throw new RobotRuntimeException("Invalid maxThreads value");
        }
    }
}
