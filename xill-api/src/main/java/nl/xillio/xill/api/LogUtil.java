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

import nl.xillio.xill.api.components.RobotID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logger class for
 *
 * Created by Anwar on 2/18/2016.
 */
public class LogUtil {
    private LogUtil() {
        // No LogUtil for you
    }

    public static final String ROBOT_LOGGER_PREFIX = "robot.";

    /**
     * Gets a new logger instance associated with a specific robot ID.
     *
     * @param robotID the ID of a robot
     * @return        an instance of the logger
     */
    public static Logger getLogger(final RobotID robotID) {
        return LoggerFactory.getLogger(ROBOT_LOGGER_PREFIX + robotID.toString());
    }
}
