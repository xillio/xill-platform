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

import me.biesaart.utils.IOUtils;
import nl.xillio.xill.XillEnvironmentImpl;
import nl.xillio.xill.api.XillEnvironment;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import static nl.xillio.xill.cli.OptionsFactory.*;

/**
 * This class represents the main command line application for Xill. It uses a lazy evaluation pattern where all
 * members are instantiated only when they are used. Before that they can be overridden using the setters.
 *
 * @author Thomas Biesaart
 */
public class XillCLI {
    private static final Logger LOGGER = LoggerFactory.getLogger(XillCLI.class);
    private static final String PROGRAM_DESCRIPTION = "Execute Xill robots from the command line.";
    private static final String PROGRAM_USAGE = "xill [-h | -v] [-q | -qq] [-i | --ignore-errors]  [-w <workingDirectory>] [-r | --robots <robotPaths>] <robotName>";

    private CommandLineParser commandLineParser;
    private CommandLine commandLine;

    private InputStream stdIn;
    private PrintStream stdOut;
    private PrintStream stdErr;
    private HelpFormatter helpFormatter;
    private OptionsFactory optionsFactory;
    private VersionPrinter versionPrinter;
    private Options options;
    private String[] args;

    private XillRobotExecutor xillRobotExecutor;
    private XillEnvironment xillEnvironment;
    private Path projectRoot;
    private Path[] includePaths;

    public static void main(String[] args) {
        XillCLI cli = new XillCLI();
        cli.setArgs(args);
        int result = cli.run().ordinal();

        // System.exit is required to send error codes to parent processes
        System.exit(result);
    }

    /**
     * Execute the application. It will use all services that are set before execution
     * and instantiate all services that have not been set yet.
     * <p>
     * If either the version or help options are set it will simply display a message.
     * Otherwise this method will execute all robots that are passed as {@link XillCLI#setArgs(String[]) arguments}.
     *
     * @return the resulting code of the execution.
     */
    public ProgramReturnCode run() {
        try {
            CommandLine cli = getCommandLine();

            // Should we print the version?
            if (cli.hasOption(OPTION_VERSION)) {
                getVersionPrinter().print();
                return ProgramReturnCode.OK;
            }

            // Should we print the help message?
            if (cli.hasOption(OPTION_HELP) || cli.getArgs().length == 0) {
                printHelp();
                return ProgramReturnCode.OK;
            }

            if (cli.hasOption(OPTION_QUIET)) {
                enableQuietLogging(Level.ERROR);
            }

            if (cli.hasOption(OPTION_VERY_QUIET)) {
                enableQuietLogging(Level.OFF);
            }

            if (cli.hasOption(OPTION_IGNORE_ERRORS)) {

            }

            for (String robot : cli.getArgs()) {
                ProgramReturnCode returnCode = tryExecute(robot);
                if ( returnCode != ProgramReturnCode.OK) {
                    return returnCode;
                }
            }
            return ProgramReturnCode.OK;
        } catch (ParseException e) {
            getStdErr().println(e.getMessage());
            printHelp();
            return ProgramReturnCode.INVALID_INPUT;
        }
    }

    private ProgramReturnCode tryExecute(String robot) throws ParseException{
        try {
            getXillRobotExecutor().execute(robot);
        } catch (RobotExecutionException e) {
            String message = e.getMessage();
            if (message == null) {
                message = e.getCause().getClass().getSimpleName();
            }
            LOGGER.error("Execution Error: " + message, e);
            return ProgramReturnCode.EXECUTION_ERROR;
        }
        return ProgramReturnCode.OK;
    }

    private void enableQuietLogging(Level level) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(level);
        ctx.updateLoggers();
    }

    private void printHelp() {
        PrintWriter printWriter = new PrintWriter(getStdOut());
        getHelpFormatter().printHelp(
                printWriter,
                120,
                PROGRAM_USAGE,
                PROGRAM_DESCRIPTION,
                getOptions(),
                4,
                8,
                getManual(),
                false
        );
        printWriter.flush();
    }

    private String getManual() {
        try (InputStream stream = getClass().getResourceAsStream("/xill/cli/manual.txt")) {
            return IOUtils.toString(stream);
        } catch (IOException ignore) {
            LOGGER.debug("The manual could not be loaded.", ignore);
            return null;
        }
    }

    public OptionsFactory getOptionsFactory() {
        if (optionsFactory == null) {
            optionsFactory = new OptionsFactory();
        }
        return optionsFactory;
    }

    public Options getOptions() {
        if (options == null) {
            options = getOptionsFactory().buildOptions();
        }
        return options;
    }

    public CommandLineParser getCommandLineParser() {
        if (commandLineParser == null) {
            commandLineParser = new DefaultParser();
        }
        return commandLineParser;
    }

    public CommandLine getCommandLine() throws ParseException {
        if (commandLine == null) {
            commandLine = getCommandLineParser().parse(getOptions(), getArgs());
        }
        return commandLine;
    }

    public InputStream getStdIn() {
        if (stdIn == null) {
            stdIn = System.in;
        }
        return stdIn;
    }

    @SuppressWarnings("squid:S106") //correct usage of System.out
    public PrintStream getStdOut() {
        if (stdOut == null) {
            stdOut = System.out;
        }
        return stdOut;
    }

    public void setStdOut(PrintStream stdOut) {
        this.stdOut = stdOut;
    }

    @SuppressWarnings("squid:S106") //correct usage of System.err
    public PrintStream getStdErr() {
        if (stdErr == null) {
            stdErr = System.err;
        }
        return stdErr;
    }

    public void setStdErr(PrintStream stdErr) {
        this.stdErr = stdErr;
    }

    public HelpFormatter getHelpFormatter() {
        if (helpFormatter == null) {
            helpFormatter = new HelpFormatter();
        }
        return helpFormatter;
    }

    public String[] getArgs() {
        if (args == null) {
            args = new String[0];
        }
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public VersionPrinter getVersionPrinter() {
        if (versionPrinter == null) {
            versionPrinter = new VersionPrinter(getStdOut());
        }
        return versionPrinter;
    }

    public XillRobotExecutor getXillRobotExecutor() throws ParseException {
        if (xillRobotExecutor == null) {
            xillRobotExecutor = new XillRobotExecutor(
                    getXillEnvironment(),
                    getProjectRoot(),
                    getIncludePaths(),
                    getStdIn(),
                    getStdOut(),
                    getStdErr()
            );
        }
        return xillRobotExecutor;
    }

    public void setXillRobotExecutor(XillRobotExecutor xillRobotExecutor) {
        this.xillRobotExecutor = xillRobotExecutor;
    }

    public XillEnvironment getXillEnvironment() {
        if (xillEnvironment == null) {
            xillEnvironment = new XillEnvironmentImpl();
        }
        return xillEnvironment;
    }

    public Path getProjectRoot() throws ParseException {
        if (projectRoot == null) {
            if (getCommandLine().hasOption(OptionsFactory.OPTION_WORKING_DIR)) {
                projectRoot = Paths.get(getCommandLine().getOptionValue(OptionsFactory.OPTION_WORKING_DIR));
            } else {
                projectRoot = Paths.get(".");
            }
        }
        return projectRoot;
    }

    public Path[] getIncludePaths() throws ParseException {
        if (includePaths == null) {
            if (getCommandLine().hasOption(OptionsFactory.OPTION_ROBOTS)) {
                String[] paths = getCommandLine().getOptionValue(OptionsFactory.OPTION_ROBOTS).split("[:;]");
                includePaths = Arrays.stream(paths).map(Paths::get).toArray(Path[]::new);
            } else {
                includePaths = new Path[0];
            }
        }
        return includePaths;
    }
}
