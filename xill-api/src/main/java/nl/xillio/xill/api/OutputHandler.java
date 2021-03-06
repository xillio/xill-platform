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

import nl.xillio.xill.api.components.Instruction;
import nl.xillio.xill.api.components.RobotID;

/**
 * This interface represents an object that can receive various events related to the output of a Xill execution.
 *
 * @author Thomas Biesaart
 */
@SuppressWarnings("squid:S1214")
public interface OutputHandler {
    String TRACE = "TRACE";
    String DEBUG = "DEBUG";
    String INFO = "INFO";
    String WARN = "WARN";
    String ERROR = "ERROR";
    /**
     * This method is called every time a message is logged to an slf4j {@link org.slf4j.Logger}.
     *
     * @param robotID    the robot for which this log is generated
     * @param level      the level
     * @param message    the message pattern
     * @param parameters the parameters
     */
    void handleLog(RobotID robotID, String level, String message, Object... parameters);

    void inspect(Instruction instruction, Throwable e);
}
