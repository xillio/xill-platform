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
package nl.xillio.xill.plugins.system.constructs;

import me.biesaart.utils.Log;
import nl.xillio.xill.api.components.Expression;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.system.exec.InputStreamReaderCallable;
import nl.xillio.xill.plugins.system.exec.ProcessDescription;
import nl.xillio.xill.plugins.system.exec.ProcessFactory;
import nl.xillio.xill.plugins.system.exec.ProcessOutput;
import nl.xillio.xill.services.inject.FactoryBuilderException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Runs an application and waits for it to complete
 */
public class ExecConstruct extends Construct {
    private static final org.slf4j.Logger LOGGER = Log.get();
    private final ProcessFactory processFactory = new ProcessFactory();

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (program, directory) -> process(program, directory, processFactory, context),
                new Argument("arguments", ATOMIC, LIST),
                new Argument("directory", NULL, ATOMIC));
    }

    MetaExpression process(final MetaExpression arguments, final MetaExpression directory, final ProcessFactory processFactory, final ConstructContext context) {
        // Get description
        ProcessDescription processDescription = parseInput(arguments, directory);

        // Start stopwatch
        StopWatch sw = new StopWatch();
        sw.start();

        // Start process
        Process process = startProcess(processFactory, processDescription);

        // Add an interrupt listener to stop the process when the robot is stopped
        Consumer<Object> processDestroyer = e -> process.destroyForcibly();
        context.addRobotInterruptListener(processDestroyer);

        // Subscribe to output
        ProcessOutput output = listenToStreams(process.getInputStream(), process.getErrorStream());

        // Wait for the process to stop
        int exitCode = -1;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            LOGGER.error("Execution interrupted: " + e.getMessage(), e);
        }

        // Remove the interrupt listener
        context.removeRobotInterruptListener(processDestroyer);

        // Stop stopwatch
        sw.stop();

        // Return results
        return parseResult(output, sw.getTime(), exitCode);
    }

    /**
     * Parse the {@link MetaExpression} input to a {@link ProcessDescription} so that the {@link ProcessFactory} can use it
     *
     * @param command   the command input
     * @param directory the directory input
     * @return the created {@link ProcessDescription}
     */
    private static ProcessDescription parseInput(final MetaExpression command, final MetaExpression directory) {

        ProcessDescription description;
        File workingDir = null;

        if (directory != NULL) {
            workingDir = new File(directory.getStringValue());
        }

        if (command.getType() == LIST) {
            // Multiple arguments
            List<MetaExpression> args = command.getValue();
            if (args.isEmpty()) {
                throw new InvalidUserInputException("The input cannot be empty.", "[]", "A list of arguments.",
                        "use System;\n\n" +
                        "var result = System.exec([\"cmd.exe\", \"/C\", \"echo test\"]);\n" +
                        "System.print(result);");
            }
            String[] commands = args.stream().map(Expression::getStringValue).toArray(String[]::new);
            description = new ProcessDescription(workingDir, commands);
            description.setFriendlyName(FilenameUtils.getName(args.get(0).getStringValue()));

        } else {
            String[] commands = new String[]{command.getStringValue()};
            description = new ProcessDescription(workingDir, commands);
            description.setFriendlyName(FilenameUtils.getName(command.getStringValue()));
        }

        return description;
    }

    /**
     * Create and start a {@link Process} using a {@link ProcessFactory} that builds the {@link Process} from a {@link ProcessDescription}
     *
     * @param factory     the factory to use
     * @param description the description of the {@link Process}
     * @return the started {@link Process}
     */
    private static Process startProcess(final ProcessFactory factory, final ProcessDescription description) {

        Process process;
        try {
            process = factory.apply(description);
        } catch (FactoryBuilderException e) {
            throw new OperationFailedException("run " + description.getFriendlyName(), e.getMessage(), "Check the executable path and name.", e);
        }

        return process;
    }

    /**
     * Start listening to the stderr and stdout streams of a {@link Process}
     *
     * @param out the stdout stream
     * @param err the stdin stream
     * @return an {@link ProcessOutput} object that holds the currently streamed output
     */
    private static ProcessOutput listenToStreams(final InputStream out, final InputStream err) {
        String output, errors;

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future outListener = executorService.submit(new InputStreamReaderCallable(out));
        Future errListener = executorService.submit(new InputStreamReaderCallable(err));

        try {
            output = (String) outListener.get();
            errors = (String) errListener.get();
        } catch (InterruptedException e) {
            throw new RobotRuntimeException("Execution interrupted: " + e.getMessage(), e);
        } catch (ExecutionException e) {
            throw new RobotRuntimeException("Execution failed: " + e.getMessage(), e);
        }

        executorService.shutdown();
        return new ProcessOutput(output, errors);
    }

    /**
     * Parse the result of running a {@link Process} to a {@link MetaExpression}
     *
     * @param output   the {@link ProcessOutput} from the streams
     * @param timeMS   the time in milliseconds it took the processor to run
     * @param exitCode The exit code of the process
     * @return the {@link MetaExpression}
     */
    private static MetaExpression parseResult(final ProcessOutput output, final long timeMS, int exitCode) {
        //functional revert of CTC-1466 to prevent breaking backwards compatibility, output as String again when releasing Xill IDE 3.2
        List<MetaExpression> errorList = Arrays.asList(output.getErrors().split("\\R")).stream().map(ExecConstruct::fromValue).collect(Collectors.toList());
        List<MetaExpression> outputList = Arrays.asList(output.getOutput().split("\\R")).stream().map(ExecConstruct::fromValue).collect(Collectors.toList());

        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();
        result.put("errors", fromValue(errorList));
        result.put("output", fromValue(outputList));
        result.put("runtime", fromValue(timeMS));
        result.put("exitCode", fromValue(exitCode));
        return fromValue(result);
    }
}
