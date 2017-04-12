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
package nl.xillio.xill.cli;

import nl.xillio.xill.api.Issue;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.XillProcessor;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.XillParsingException;
import nl.xillio.xill.api.io.SimpleIOStream;
import org.eclipse.emf.common.util.WrappedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;

/**
 * This class is responsible for the execution of Xill robots.
 * It uses a {@link XillEnvironment} to build a {@link XillProcessor} to run a robot.
 *
 * @author Thomas Biesaart
 */
public class XillRobotExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(XillRobotExecutor.class);
    private final XillEnvironment xillEnvironment;
    private final Path projectRoot;
    private final Path[] includePaths;
    private final InputStream stdIn;
    private final PrintStream stdOut;
    private final PrintStream stdErr;

    /**
     * Create a new {@link XillRobotExecutor} for a fixed project and environment.
     *
     * @param xillEnvironment The environment that should be used for plugins.
     * @param projectRoot     This is the root folder of your project. Robots will be resolved with this folder as their
     *                        base.
     * @param stdIn           The input stream for the robot. This stream will be exposed to the robot in the
     *                        <code>argument</code> parameter's <code>input</code> field.
     * @param stdOut          The output stream for the robot. This stream will be exposed to the robot in the
     *                        <code>argument</code> parameter's <code>output</code> field.
     * @param stdErr          The error stream for the robot. This stream will be exposed to the robot in the
     *                        <code>argument</code> parameter's <code>error</code> field.
     */
    public XillRobotExecutor(XillEnvironment xillEnvironment, Path projectRoot, Path[] includePaths, InputStream stdIn, PrintStream stdOut, PrintStream stdErr) {
        this.xillEnvironment = xillEnvironment;
        this.projectRoot = projectRoot;
        this.includePaths = includePaths;
        this.stdIn = stdIn;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
    }

    /**
     * Execute a robot. Robot can be specified by either their fully qualified names
     *
     * @param robotName The robot fully qualified name
     * @throws RobotExecutionException when the robot does not complete successfully.
     */
    public void execute(String robotName) throws RobotExecutionException {
        try (XillProcessor processor = compile(robotName)) {
            processor.getRobot().process(processor.getDebugger());
        } catch (WrappedException e) {
            throw new RobotExecutionException(e.getCause().getMessage(), e.getCause());
        } catch (Exception e) {
            throw new RobotExecutionException(e.getMessage(), e);
        }
    }

    private XillProcessor compile(String robotName) throws RobotExecutionException {
        XillProcessor processor = buildProcessor(robotName);
        List<Issue> issues = compile(processor);
        issues.forEach(this::log);
        processor.getRobot().setArgument(buildArgument());
        return processor;
    }

    private void log(Issue issue) {
        switch (issue.getSeverity()) {
            case ERROR:
                LOGGER.error("Error in {} at line {}:\n\t{}", issue.getRobot().getURL(), issue.getLine(), issue.getMessage());
                break;
            case WARNING:
                LOGGER.warn("Warning in {} at line {}:\n\t{}", issue.getRobot().getURL(), issue.getLine(), issue.getMessage());
                break;
            case INFO:
                LOGGER.info("{} at line {}:\n\t{}", issue.getRobot().getURL(), issue.getLine(), issue.getMessage());
                break;
        }
    }

    private List<Issue> compile(XillProcessor processor) throws RobotExecutionException {
        try {
            return processor.compile();
        } catch (IOException e) {
            throw new RobotExecutionException("Fatal error during compilation.\n" + e.getMessage(), e);
        } catch (XillParsingException e) {
            throw new RobotExecutionException("Compile Error: " + e.getMessage(), e);
        }
    }

    private XillProcessor buildProcessor(String robotName) throws RobotExecutionException {
        try {
            return xillEnvironment.buildProcessor(
                    projectRoot,
                    robotName,
                    includePaths
            );
        } catch (IOException e) {
            throw new RobotExecutionException(
                    "Failed to initialize Xill.\n" + e.getMessage(),
                    e
            );
        }
    }

    private MetaExpression buildArgument() {
        LinkedHashMap<String, MetaExpression> data = new LinkedHashMap<>(3);

        data.put("input", fromValue(new SimpleIOStream(stdIn, "InputStream")));
        data.put("output", fromValue(new SimpleIOStream(stdOut, "OutputStream")));
        data.put("error", fromValue(new SimpleIOStream(stdErr, "ErrorStream")));

        return fromValue(data);
    }
}
