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
package nl.xillio.xill.webservice.xill;

import nl.xillio.xill.api.*;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Robot;
import nl.xillio.xill.api.errors.XillParsingException;
import nl.xillio.xill.api.io.SimpleIOStream;
import nl.xillio.xill.webservice.exceptions.RobotAbortException;
import nl.xillio.xill.webservice.exceptions.CompileException;
import nl.xillio.xill.webservice.exceptions.RobotNotFoundException;
import nl.xillio.xill.webservice.model.Runtime;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.*;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;
import static nl.xillio.xill.api.components.MetaExpression.extractValue;

/**
 * Implementation of the {@link Runtime}.
 *
 * The expected usage pattern is calling {@link #compile(Path, String)} once before being
 * able to call {@link #runRobot(Map)} as often as required. Running robots can be aborted
 * from a different thread by calling {@link #abortRobot()}. This class does not do any checking
 * of state, meaning that {@link #runRobot(Map)} can be called before calling {@link #compile(Path, String)}
 * and {@link #abortRobot()} can be called when no robot is running, but these cases will result
 * in undefined behaviour.
 *
 * Since a robot has to be compiled once for each run, this class recompiles its robots asynchronously
 * after each run. Any errors occurring during recompilation are only logged since it is assumed that
 * the robot does not change after {@link #compile(Path, String)} has been called.
 *
 * This class is designed to be pooled, meaning that it can run different robots. {@link #compile(Path, String)}
 * should be called to change the robot this runtime is able to run.
 *
 * @author Geert Konijnendijk
 */
@Component("xillRuntimeImpl")
@Scope("prototype")
public class RuntimeImpl implements Runtime, DisposableBean {
    private static final Logger LOGGER = me.biesaart.utils.Log.get();

    private XillEnvironment xillEnvironment;
    private OutputHandler outputHandler;

    // Set after compile is called
    private XillProcessor xillProcessor;
    private XillThreadFactory xillThreadFactory;

    // Future for asynchronous recompiling
    private Future<?> compileSuccess;
    private ThreadPoolTaskExecutor compileExecutor;

    @Value("${xillRuntime.abortTimeoutMillis:300000}")
    private long abortTimeoutMillis;

    /**
     * Creates a new runtime.
     *
     * @param xillEnvironment the xill environment used for running robots. Is private to this runtime and will be closed when the runtime is closed.
     * @param outputHandler the handler for robot output
     * @param compileExecutor the executor for asynchronously recompiling robots after a run
     */
    @Inject
    public RuntimeImpl(XillEnvironment xillEnvironment, OutputHandler outputHandler, @Qualifier("robotCompileThreadPool") ThreadPoolTaskExecutor compileExecutor) {
        this.xillEnvironment = xillEnvironment;
        this.outputHandler = outputHandler;

        // Create a thread factory that can be cleaned up
        xillThreadFactory = new CleaningThreadFactory();
        this.xillEnvironment.setXillThreadFactory(xillThreadFactory);

        // Create an executor for asynchronous recompilation
        this.compileExecutor = compileExecutor;
    }

    @Override
    public void compile(Path workDirectory, String robotFQN) throws CompileException, RobotNotFoundException {

        String robotPath = robotFQN.replace('.', File.separatorChar) + XillEnvironment.ROBOT_EXTENSION;

        Path resolvedPath = workDirectory.resolve(robotPath);
        if (!resolvedPath.toFile().exists()) {
            throw new RobotNotFoundException("The robot does not exists: " + resolvedPath.toString());
        }
        try {
            xillProcessor = xillEnvironment.buildProcessor(workDirectory, resolvedPath);
            xillProcessor.setOutputHandler(outputHandler);
            // Ignore all errors since they will be picked up by the output handler
            xillProcessor.getDebugger().setErrorHandler(new RobotErrorHandler());

            // Compile to check for errors in the robot
            xillProcessor.compile();

            // Mark the compilation as success
            compileSuccess = ConcurrentUtils.constantFuture(true);
        } catch (IOException | XillParsingException e) {
            throw new CompileException("Failed to compile robot", e);
        }
    }

    @Override
    public Object runRobot(Map<String, Object> parameters) {
        // Do nothing when no robot has been compiled yet
        if (compileSuccess == null) {
            return null;
        }

        // Check if the previous compilation succeeded or wait for it if it is in progress
        try {
            compileSuccess.get();
        } catch (InterruptedException e) {
            LOGGER.error("Waiting for robot compilation was interrupted", e);
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            // We do not throw the exception since the robot has already successfully compiled during the call to compile()
            LOGGER.error("Error compiling robot, if this robot has changed, a new worker should be allocated", e);
            // Rethrow the cause as a runtime exception
            ConcurrentUtils.handleCauseUnchecked(e);
            return null;
        }

        Robot processableRobot = xillProcessor.getRobot();

        processableRobot.setArgument(MetaExpression.parseObject(parameters, this::getStream));
        InstructionFlow<MetaExpression> result = processableRobot.process(xillProcessor.getDebugger());

        Object parsedResult = result.hasValue() ? parseResult(result.get()) : null;

        // Asynchronously recompile the robot
        compileSuccess = compileExecutor.submit(xillProcessor::compile);

        return parsedResult;
    }

    /**
     * Creates a MetaExpression containing a stream from an object.
     *
     * @param input the object to wrap in a MetaExpression
     * @return a MetaExpression if {@code input} is an {@link InputStream}, null otherwise
     */
    private MetaExpression getStream(Object input) {
        if (input instanceof InputStream) {
            return fromValue(new SimpleIOStream((InputStream) input, "Http File Stream"));
        }
        return null;
    }

    /**
     * This method will intercept value extraction from MetaExpression if it contains an {@link java.io.InputStream}.
     *
     * @param metaExpression the expression
     * @return an InputStream if the expression contains a stream. Otherwise the result of {@link MetaExpression#extractValue(MetaExpression)}
     */
    private Object parseResult(MetaExpression metaExpression) {
        try {
            if (metaExpression.getBinaryValue().hasInputStream()) {
                return metaExpression.getBinaryValue().getInputStream();
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to read data from the robot result stream. Falling back to regular extraction.", e);
        }
        return extractValue(metaExpression);
    }

    @Override
    public void abortRobot() {
        Debugger debugger = xillProcessor.getDebugger();
        debugger.stop();

        CompletableFuture<Void> robotStopFuture = new CompletableFuture<>();

        debugger.getOnRobotStop().addListener(e -> robotStopFuture.complete(null));

        try {
            robotStopFuture.get(abortTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.info("Stopping robot was interrupted", e);
        } catch (ExecutionException e) {
            throw new RobotAbortException("Exception occurred while waiting for robot stop", e);
        } catch (TimeoutException e) {
            throw new RobotAbortException("Could not abort the robot within the configured timeout", e);
        }
    }

    @Override
    public void close() {
        xillEnvironment.close();
        try {
            xillThreadFactory.close();
        } catch (Exception e) {
            LOGGER.error("Could not close Xill threads", e);
        }
    }

    @Override
    public void destroy() {
        close();
    }

    /**
     * @return the maximum time a call to abort a robot will block
     */
    public long getAbortTimeoutMillis() {
        return abortTimeoutMillis;
    }

    /**
     * @param abortTimeoutMillis the maximum time a call to abort a robot will block
     */
    public void setAbortTimeoutMillis(long abortTimeoutMillis) {
        this.abortTimeoutMillis = abortTimeoutMillis;
    }
}
