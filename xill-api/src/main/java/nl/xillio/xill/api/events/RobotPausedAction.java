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

import nl.xillio.xill.api.components.Instruction;
import nl.xillio.xill.api.components.RobotID;

/**
 * This class contains all information on what happened when a robot got paused.
 */
public class RobotPausedAction {
    private final Instruction instruction;

    /**
     * Instantiate the action and pull all data from an instruction
     *
     * @param instruction the instruction to pull the data from
     */
    public RobotPausedAction(final Instruction instruction) {
        this.instruction = instruction;
    }

    /**
     * @return The line number the robot paused on
     */
    public int getLineNumber() {
        return instruction.getLineNumber();
    }

    /**
     * @return The robotID that caused the pause
     */
    public RobotID getRobotID() {
        return instruction.getRobotID();
    }
}
