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
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.cli.services.XillEnvironmentFactory;
import nl.xillio.xill.cli.services.XillExecutionService;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * This command will initialize the execution service.
 *
 * @author Thomas Biesaart
 */
@Command(value = "init", description = "Initialize the execution service")
public class InitCommand {
    private static final Logger LOGGER = Log.get();
    private final XillEnvironmentFactory factory;
    private final XillExecutionService executionService;

    @Inject
    public InitCommand(XillEnvironmentFactory factory, XillExecutionService executionService) {
        this.factory = factory;
        this.executionService = executionService;
    }

    @CommandTask
    public void run(Arguments arguments) {
        String core = getOrXillHome(arguments.isEmpty() ? null : arguments.get(0));
        Path coreFolder = getPath(core);

        Path[] paths = arguments.stream().skip(1).map(this::getPath).toArray(Path[]::new);

        try {
            buildEnv(coreFolder, paths);
        } catch (NoSuchFileException e) {
            LOGGER.error("Could not find folder: " + e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error("Failed to build execution environment: " + e.getMessage(), e);
        }
    }

    private void buildEnv(Path coreFolder, Path... paths) throws IOException {
        LOGGER.info("Loading xill from {}", coreFolder.toAbsolutePath());
        if (paths.length > 0) {
            LOGGER.info("Plugins: {}", Arrays.toString(paths));
        }

        XillEnvironment environment = factory.buildFor(coreFolder, paths);
        environment.loadPlugins();
        executionService.init(environment);
    }

    private Path getPath(String path) {
        try {
            return Paths.get(path);
        } catch (InvalidPathException e) {
            throw new IllegalUserInputException(path + " is not a valid path", e);
        }
    }

    private String getOrXillHome(String fallbackValue) {
        if (fallbackValue != null) {
            return fallbackValue;
        }

        String result = System.getenv("XILL_HOME");

        if (result != null) {
            return result;
        }

        throw new IllegalUserInputException("No xill implementation was provided. Provide one by running 'init <xillFolder>' or setting the 'XILL_HOME' environmental variable.");
    }
}
