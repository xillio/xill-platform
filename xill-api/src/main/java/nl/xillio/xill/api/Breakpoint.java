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
 * This class represents a breakpoint in the robot code.
 */
public class Breakpoint {
    private final RobotID robotID;
    private final int lineNumber;

    /**
     * Creates a breakpoint on a robot at a the provided line number.
     *
     * @param robotID    the id of the robot on which to apply this breakpoint
     * @param lineNumber the line number on which the breakpoint will be set
     */
    public Breakpoint(final RobotID robotID, final int lineNumber) {
        this.robotID = robotID;
        this.lineNumber = lineNumber;
    }

    /**
     * Returns whether the breakpoint was reached.
     *
     * @param next the instruction to check
     * @return whether there is a breakpoint on the instruction
     */
    public boolean matches(final Instruction next) {
        return next.getRobotID().equals(robotID) &&
                next.getLineNumber() == lineNumber;
    }
}
