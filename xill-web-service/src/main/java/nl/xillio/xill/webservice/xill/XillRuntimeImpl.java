package nl.xillio.xill.webservice.xill;

import nl.xillio.xill.api.OutputHandler;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.XillProcessor;
import nl.xillio.xill.api.XillThreadFactory;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Robot;
import nl.xillio.xill.api.errors.XillParsingException;
import nl.xillio.xill.api.io.SimpleIOStream;
import nl.xillio.xill.webservice.exceptions.XillCompileException;
import nl.xillio.xill.webservice.exceptions.XillInvalidStateException;
import nl.xillio.xill.webservice.model.XillRuntime;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;
import static nl.xillio.xill.api.components.MetaExpression.extractValue;

/**
 * Implementation of the {@link XillRuntime}
 *
 * @author Geert Konijnendijk
 */
@Component("xillRuntimeImpl")
@Scope("prototype")
public class XillRuntimeImpl implements XillRuntime, DisposableBean {
    private static final Logger LOGGER = me.biesaart.utils.Log.get();

    private XillEnvironment xillEnvironment;
    private OutputHandler outputHandler;

    // Set after compile is called
    private XillProcessor xillProcessor;
    private XillThreadFactory xillThreadFactory;

    // Future for asynchronous recompiling
    private Future<?> compileSuccess;
    private ThreadPoolTaskExecutor compileExecutor;

    @Inject
    public XillRuntimeImpl(XillEnvironment xillEnvironment, OutputHandler outputHandler, @Qualifier("robotCompileThreadPool") ThreadPoolTaskExecutor compileExecutor) {
        this.xillEnvironment = xillEnvironment;
        this.outputHandler = outputHandler;

        // Create a thread factory that can be cleaned up
        xillThreadFactory = new CleaningXillThreadFactory();
        this.xillEnvironment.setXillThreadFactory(xillThreadFactory);

        // Create an executor for asynchronous recompilation
        this.compileExecutor = compileExecutor;
    }

    @Override
    public synchronized void compile(Path workDirectory, Path robotPath) throws XillCompileException {
        try {
            xillProcessor = xillEnvironment.buildProcessor(workDirectory, workDirectory.resolve(robotPath));
            xillProcessor.setOutputHandler(outputHandler);
            // Ignore all errors since they will be picked up by the output handler
            xillProcessor.getDebugger().setErrorHandler(e -> { });

            // Compile to check for errors in the robot
            xillProcessor.compile();

            // Mark the compilation as success
            compileSuccess = ConcurrentUtils.constantFuture(true);
        } catch (IOException | XillParsingException e) {
            throw new XillCompileException("Failed to compile robot", e);
        }
    }

    @Override
    public synchronized Object runRobot(Map<String, Object> parameters) {
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
     * Create a MetaExpression containing a stream from an object
     * @param input The object to wrap in a MetaExpression
     * @return A MetaExpression if {@code input} is an {@link InputStream}, null otherwise
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
    public synchronized void abortRobot() {
        xillProcessor.getDebugger().stop();
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
}
