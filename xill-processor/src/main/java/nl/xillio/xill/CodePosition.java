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
package nl.xillio.xill;

import nl.xillio.xill.api.components.RobotID;

/**
 * This class represents a location in the source
 */
public class CodePosition {

    private final RobotID robot;
    private final int lineNumber;

    /**
     * Create a new {@link CodePosition}-object.
     *
     * @param robot         the robot from which we would like to know the position.
     * @param lineNumber    the line number on which code has stopped execution.
     */
    public CodePosition(final RobotID robot, final int lineNumber) {
        this.robot = robot;
        this.lineNumber = lineNumber;
    }

    /**
     * Get the line number of the robot.
     *
     * @return the lineNumber
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Get the robot.
     *
     * @return the robot
     */
    public RobotID getRobotID() {
        return robot;
    }

    @Override
    public String toString() {
        return robot.getPath().getAbsolutePath() + ":" + lineNumber;
    }
}
