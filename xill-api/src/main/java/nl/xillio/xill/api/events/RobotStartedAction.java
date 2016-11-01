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
package nl.xillio.xill.api.events;

import nl.xillio.xill.api.components.Robot;
import java.util.UUID;

/**
 * This class represents the object passed to all listeners when a robot is started.
 */
public class RobotStartedAction {

    private final Robot robot;

    /**
     * Default Constructor.
     *
     * @param robot the robot that started
     */
    public RobotStartedAction(final Robot robot) {
        this.robot = robot;
    }

    /**
     * @return compiler serial ID
     */
    public UUID getCompilerSerialID() {
        return robot.getCompilerSerialId();
    }

}
