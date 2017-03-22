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
import nl.xillio.plugins.XillPlugin;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.OutputHandler;
import nl.xillio.xill.api.components.*;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.loaders.AbstractRobotLoader;
import nl.xillio.xill.services.files.FileResolver;
import nl.xillio.xill.services.files.FileResolverImpl;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static nl.xillio.xill.api.components.ExpressionDataType.ATOMIC;
import static nl.xillio.xill.api.components.ExpressionDataType.LIST;

/**
 * This class represents calling another robot multiple times in a separate threads.
 */
public class RunBulkExpression implements Processable {

    private static final Logger LOGGER = Log.get();
    private Path workingDirectory;
    private final Processable path;
    private final RobotID robotID;
    private final List<XillPlugin> plugins;
    private Processable argument;
    private final FileResolver resolver;
    private Processable optionsProcessable;
    private RunBulkOptions options;
    private final OutputHandler outputHandler;
    private final AbstractRobotLoader loader;

    private int maxThreadsVal;

    /**
     * Create a new {@link RunBulkExpression}.
     *
     * @param workingDirectory  the working directory
     * @param path              the path of the called bot
     * @param robotID           the root robot of this tree
     * @param plugins           the current plugin loader
     * @param outputHandler
     * @param loader
     */
    public RunBulkExpression(final Path workingDirectory, final Processable path, final RobotID robotID, final List<XillPlugin> plugins, OutputHandler outputHandler, AbstractRobotLoader loader) {
        this.workingDirectory = workingDirectory;
        this.path = path;
        this.robotID = robotID;
        this.plugins = plugins;
        this.outputHandler = outputHandler;
        this.loader = loader;
        resolver = new FileResolverImpl();
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) {
        String otherRobot = path.process(debugger).get().getStringValue();

        LOGGER.debug("Evaluating runBulk for " + otherRobot);
        if (loader.getRobot(otherRobot) == null) {
            throw new RobotRuntimeException("Called robot " + otherRobot + " does not exist.");
        }

        options = new RunBulkOptions(optionsProcessable);
        int robotRunCount = runBulk(debugger, otherRobot, loader);

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
     * Run the called robot multiple times.
     *
     * @return The number of robot runs
     */
    private int runBulk(final Debugger debugger, final String calledRobotQualifiedName, final AbstractRobotLoader loader) {
        // Evaluate argument
        if (argument == null) {
            return 0; // Nothing to do
        }

        maxThreadsVal = options.getMaxThreadsVal();
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
        RunBulkControl control = new RunBulkControl(debugger, calledRobotQualifiedName, loader);

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
     * Create {@link RunBulkOptions#getMaxThreadsVal()} worker threads and start them.
     *
     * @param queue The queue to pass to the workers
     * @param control The control to pass to the workers
     * @return The worker threads
     */
    private List<Thread> spawnWorkers(BlockingQueue<MetaExpression> queue, RunBulkControl control) {
        // Start working threads
        List<Thread> workingThreads = new LinkedList<>();
        WorkerRobotFactory robotFactory = new WorkerRobotFactory(workingDirectory, robotID, plugins, outputHandler);
        for (int i = 0; i < maxThreadsVal; i++) {
            Thread worker = new WorkerThread(queue, control, options.shouldStopOnError(), robotFactory);
            worker.start();
            workingThreads.add(worker);
        }
        return workingThreads;
    }

    /**
     * Wait until either the queue is empty or we should stop due to some other reason signalled by {@link RunBulkControl}.
     *
     * @param queue The queue to wait for to become empty
     * @param control Stop waiting when the control signals to stop
     */
    private void waitUntilDone(BlockingQueue<MetaExpression> queue, RunBulkControl control) {
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
     * Join all threads and wait until they are joined.
     *
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
        this.optionsProcessable = options;
    }


}
