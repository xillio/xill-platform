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
package nl.xillio.xill.api.errors;

import nl.xillio.xill.api.LanguageFactory;
import nl.xillio.xill.api.components.RobotID;

/**
 * This {@link Exception} is generally thrown when the {@link LanguageFactory} was unable to generate a program tree from the provided token tree.
 */
public class XillParsingException extends Exception {
    private final int line;
    private final RobotID robot;

    /**
     * Creates a {@link XillParsingException} with a message.
     *
     * @param message the message to display
     * @param line    the line where the error occurred
     * @param robot   the robot that couldn't be parsed
     */
    public XillParsingException(final String message, final int line, final RobotID robot) {
        super(message + (line > 0 ? " (line " + line + ")" : "")); // Only show the line number if it is > 0.
        this.line = line;
        this.robot = robot;
    }

    /**
     * Creates a {@link XillParsingException} with a message and a cause.
     *
     * @param message the message to display
     * @param line    the line where the error occurred
     * @param robot   the robot that couldn't be parsed
     * @param e       the exception that caused this
     */
    public XillParsingException(final String message, final int line, final RobotID robot, final Exception e) {
        super(message, e);
        this.line = line;
        this.robot = robot;
    }

    /**
     * @return the line where the exception occurred
     */
    public int getLine() {
        return line;
    }

    /**
     * @return the robot in which the exception occured
     */
    public RobotID getRobot() {
        return robot;
    }

}
