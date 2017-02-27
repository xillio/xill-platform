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
package nl.xillio.xill.components.instructions;

import nl.xillio.xill.CodePosition;
import nl.xillio.xill.api.components.RobotID;

/**
 * This is the base interface for all instructions. An instruction generally represents code that would qualify as a minimal syntactically correct program.
 */
public abstract class Instruction implements nl.xillio.xill.api.components.Instruction {

    private CodePosition position;
    private InstructionSet hostInstruction;

    @Override
    public RobotID getRobotID() {
        return position.getRobotID();
    }

    @Override
    public int getLineNumber() {
        return position.getLineNumber();
    }

    /**
     * Set the code position of this instruction.
     *
     * <b>Note!</b> This value can only be set once!
     *
     * @param position The position of the instruction in the code to which it needs to be set.
     */
    public void setPosition(final CodePosition position) {
        if (this.position != null) {
            return;
        }

        this.position = position;
    }

    protected CodePosition getPosition() {
        return position;
    }

    @Override
    public String toString() {
        String path = getRobotID().getPath().toURI().relativize(getRobotID().getPath().toURI()).getPath();

        return path + ":" + getLineNumber() + " > " + getClass().getSimpleName();
    }

    /**
     * @return true if debugging should be prevented
     */
    public boolean preventDebugging() {
        return false;
    }

    @Override
    public void close() throws Exception {

    }

    public InstructionSet getHostInstruction() {
        return hostInstruction;
    }

    public void setHostInstruction(InstructionSet hostInstruction) {
        if (this.hostInstruction != null) {
            throw new IllegalStateException("The host instruction set has already been set!");
        }
        this.hostInstruction = hostInstruction;
    }
}
