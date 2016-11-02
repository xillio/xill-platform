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
package nl.xillio.migrationtool.elasticconsole;

/**
 * This class represents all information about a log event.
 */
public class RobotLogMessage {

    private final String level;
    private final String message;

    /**
     * Creates a new log message.
     *
     * @param level
     * @param message the message to log
     */
    public RobotLogMessage(final String level, final String message) {
        this.level = level;
        this.message = message;
    }

    /**
     * Returns the level.
     *
     * @return the level
     */
    public String getLevel() {
        return level;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
}
