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
package nl.xillio.xill.cli.commands;

import com.google.inject.Inject;
import me.biesaart.utils.Log;
import me.biesaart.wield.annotations.Command;
import me.biesaart.wield.annotations.CommandTask;
import me.biesaart.wield.error.IllegalUserInputException;
import me.biesaart.wield.input.Arguments;
import me.biesaart.wield.input.Flag;
import me.biesaart.wield.input.Flags;
import nl.xillio.xill.api.Issue;
import nl.xillio.xill.cli.services.CompileException;
import nl.xillio.xill.cli.services.XillExecutionService;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This command allows the user the run a robot.
 *
 * @author Thomas Biesaart
 */
@Command(value = "run", description = "Run a robot")
public class RunCommand {
    private static final Logger LOGGER = Log.get();
    private final XillExecutionService executionService;
    private final InitCommand initCommand;

    @Inject
    public RunCommand(XillExecutionService executionService, InitCommand initCommand) {
        this.executionService = executionService;
        this.initCommand = initCommand;
    }

    @CommandTask
    public void run(Arguments arguments, Flags flags) {
        if (arguments.isEmpty()) {
            throw new IllegalUserInputException("Usage: run <robotName>");
        }

        if (!executionService.isInitialized()) {
            initCommand.run(new Arguments());
        }

        // Get the data
        String robotPath = arguments.get(0);
        Flag projectPath = flags.hasFlag("p") ? flags.get("p") : null;

        Path project = getProjectPath(projectPath);
        Path robot = getRobotPath(robotPath, project);

        try {
            executionService.run(robot, project)
                    .forEach(this::logIssue);
        } catch (CompileException e) {
            compileError(e.getRobot().getPath().getAbsolutePath(), e.getLine(), e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error("Failed to start robot: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("squid:UnusedPrivateMethod") // Sonar still doesn't do method references
    private void logIssue(Issue issue) {
        compileError(issue.getRobot().getPath().getAbsolutePath(), issue.getLine(), issue.getMessage(), null);
    }

    private void compileError(String robotPath, int line, String message, Throwable e) {
        String fullMessage = String.format("Compile error: %nRobot: %s%nLine: %d%nMessage: %s", robotPath, line, message);

        if (e == null) {
            LOGGER.error(fullMessage);
        } else {
            LOGGER.error(fullMessage, e);
        }
    }

    private Path getProjectPath(Flag projectPath) {
        if (projectPath == null) {
            return Paths.get(".");
        }

        return Paths.get(projectPath.stringValue());
    }

    private Path getRobotPath(String robotPath, Path projectPath) {
        // Try to run by fqn
        Path fqnPath = projectPath.resolve(fqnToPath(robotPath));

        if (Files.exists(fqnPath)) {
            return fqnPath;
        }

        // Try to run by robot path
        Path robot = projectPath.resolve(robotPath);

        if (Files.exists(robot)) {
            LOGGER.info("Detected a file path. Please consider using the fully qualified name.");
            return robot;
        }

        throw new IllegalUserInputException("No robot found for [" + robotPath + "]");
    }

    private String fqnToPath(String robotPath) {
        return robotPath.replaceAll("\\.", "/") + ".xill";
    }
}
