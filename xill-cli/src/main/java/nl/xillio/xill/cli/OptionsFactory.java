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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * This class contains all options of the {@link XillCLI Xill Command Line Client}.
 * @author Thomas Biesaart
 */
public class OptionsFactory {
    public static final String OPTION_HELP = "h";
    public static final String OPTION_VERSION = "v";
    public static final String OPTION_WORKING_DIR = "w";
    public static final String OPTION_QUIET = "q";
    public static final String OPTION_VERY_QUIET = "qq";
    public static final String OPTION_ROBOTS = "r";
    public static final String OPTION_IGNORE_ERRORS = "i";

    public static final String CANNOT_BE_USED_IN_COMBINATION = "\nThis option cannot be used in combination with -";

    public Options buildOptions() {
        OptionGroup logging = new OptionGroup()
                .addOption(
                        Option.builder(OPTION_QUIET)
                                .longOpt("quiet")
                                .desc(
                                        "Remove all logging from the output except errors. " +
                                                "Enable this option if you need clean logs." +
                                                 CANNOT_BE_USED_IN_COMBINATION + OPTION_VERY_QUIET + "."
                                )
                                .build()
                ).addOption(
                        Option.builder(OPTION_VERY_QUIET)
                                .longOpt("silent")
                                .desc(
                                        "Remove all logging from the output including errors. " +
                                                "Enable this option if you are using the output for data streams." +
                                                CANNOT_BE_USED_IN_COMBINATION + OPTION_QUIET + "."
                                )
                                .build()
                );

        OptionGroup action = new OptionGroup()
                .addOption(
                        Option.builder(OPTION_HELP)
                                .longOpt("help")
                                .desc(
                                        "Display this message." +
                                                CANNOT_BE_USED_IN_COMBINATION + OPTION_VERSION + "."
                                )
                                .build()
                ).addOption(
                        Option.builder(OPTION_VERSION)
                                .longOpt("version")
                                .desc(
                                        "Display version information about the Xill command line interface." +
                                                CANNOT_BE_USED_IN_COMBINATION + OPTION_HELP + "."
                                )
                                .build()
                );

        return new Options()
                .addOptionGroup(logging)
                .addOptionGroup(action)
                .addOption(
                        Option.builder(OPTION_WORKING_DIR)
                                .longOpt("working-directory")
                                .desc("Set the working directory for the Xill robots that should be run.")
                                .argName("workingDirectory")
                                .hasArg()
                                .build()
                )
                .addOption(
                        Option.builder(OPTION_ROBOTS)
                                .longOpt("robots")
                                .desc("Set the directories to include when running Xill robots. " +
                                        "The directories should be separated by a (semi)colon (':' or ';').")
                                .argName("robotPaths")
                                .hasArg()
                                .build()
                )
                .addOption(
                        Option.builder(OPTION_IGNORE_ERRORS)
                                .longOpt("ignore-errors")
                                .desc("Set the robot to continue even if errors occur.")
                                .build()
                );
    }
}
