package nl.xillio.xill.webservice.xill;

import nl.xillio.xill.api.*;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Robot;
import nl.xillio.xill.api.errors.XillParsingException;
import nl.xillio.xill.api.io.SimpleIOStream;
import nl.xillio.xill.webservice.exceptions.XillCompileException;
import nl.xillio.xill.webservice.exceptions.XillInvalidStateException;
import nl.xillio.xill.webservice.model.XillRuntime;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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

    // Whether a robot is currently running
    private boolean isRunning;

    // Set after compile is called
    private XillProcessor xillProcessor;
    private XillThreadFactory xillThreadFactory;

    @Inject
    public XillRuntimeImpl(XillEnvironment xillEnvironment, OutputHandler outputHandler) {
        this.xillEnvironment = xillEnvironment;
        this.outputHandler = outputHandler;

        // Create a thread factory that can be cleaned up
        xillThreadFactory = new CleaningXillThreadFactory();
        this.xillEnvironment.setXillThreadFactory(xillThreadFactory);
    }

    @Override
    public void compile(Path workDirectory, Path robotPath) throws XillCompileException {
        try {
            xillProcessor = xillEnvironment.buildProcessor(workDirectory, workDirectory.resolve(robotPath));
            xillProcessor.setOutputHandler(outputHandler);
            // Ignore all errors since they will be picked up by the output handler
            xillProcessor.getDebugger().setErrorHandler(e -> { });

            // Compile to check for errors in the robot
            List<Issue> issues = xillProcessor.compile();
            if (!issues.isEmpty()) {
                throw new XillCompileException("Compilation issues found", issues);
            }
        } catch (IOException | XillParsingException e) {
            throw new XillCompileException("Failed to compile robot", e);
        }
    }

    @Override
    public Object runRobot(Map<String, Object> parameters) {
        isRunning = true;

        try {
            // We need to compile here to allow the runtime to run a single robot multiple times
            xillProcessor.compile();
        } catch (IOException | XillParsingException e) {
            // We do not throw the exception since the robot has already successfully compiled during the call to compile()
            LOGGER.error("Error compiling robot, if this robot has changed, a new worker should be allocated", e);
            isRunning = false;
            return null;
        }

        Robot processableRobot = xillProcessor.getRobot();

        processableRobot.setArgument(MetaExpression.parseObject(parameters, this::getStream));
        InstructionFlow<MetaExpression> result = processableRobot.process(xillProcessor.getDebugger());

        isRunning = false;
        if (result.hasValue()) {
            return parseResult(result.get());
        } else {
            return null;
        }
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
    public void abortRobot() throws XillInvalidStateException {
        if (!isRunning) {
            throw new XillInvalidStateException("Tried to abort a robot while it was not running");
        }
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
