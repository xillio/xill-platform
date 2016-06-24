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
package nl.xillio.xill.api;

import nl.xillio.xill.api.components.Robot;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.errors.XillParsingException;

/**
 * This interface represents a factory that can build a program tree.
 *
 * @param <T> the input token for this factory
 */
public interface LanguageFactory<T> {
    /**
     * Processes a token into a {@link Robot} which can be processed.
     *
     * @param token   the token that should be processed
     * @param robotID the id that should be set for the robot
     * @throws XillParsingException when the robot cannot be parsed
     */
    public void parse(final T token, final RobotID robotID) throws XillParsingException;

    /**
     * Finishes compiling.
     *
     * @throws XillParsingException when the robot cannot be compiled
     */
    public void compile() throws XillParsingException;

    /**
     * Gets a compiled robot.
     *
     * @param token the token used to compile the robot
     * @return the compiled robot for that token or null if none was found
     */
    public Robot getRobot(final T token);
}
