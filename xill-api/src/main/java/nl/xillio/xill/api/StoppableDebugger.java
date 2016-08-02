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

import nl.xillio.xill.api.components.EventEx;
import nl.xillio.xill.api.components.Instruction;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.ErrorHandlingPolicy;

import java.util.List;
import java.util.Stack;

/**
 * This class represents a debugger that can be stopped (this is only behaviour that is supported).
 * Debugger can stop if error occurs (this is optional).
 */
public class StoppableDebugger extends NullDebugger {

    private final Stack<Instruction> currentStack = new Stack<>();
    private ErrorHandlingPolicy errorHandlingPolicy;
    private boolean stop = false;
    private boolean errorOccurred = false;
    private boolean stopOnError = false;
    private final Debugger parent;

    public StoppableDebugger(Debugger parent) {
        this.parent = parent;
    }

    @Override
    public void startInstruction(final nl.xillio.xill.api.components.Instruction instruction) {
        Instruction internalInstruction = (Instruction) instruction;
        currentStack.add(internalInstruction);
    }

    @Override
    public void endInstruction(final nl.xillio.xill.api.components.Instruction instruction, final InstructionFlow<MetaExpression> result) {
        currentStack.pop();
    }

    @Override
    public int getStackDepth() {
        int stackSize = currentStack.size();
        // The stack size is 0 for variable initializers in included robots, those should be on depth 0 too
        return stackSize > 0 ? stackSize - 1 : 0;
    }

    @Override
    public void stop() {
        stop = true;
    }

    @Override
    public boolean shouldStop() {
        return stop;
    }

    @Override
    public void handle(Throwable e) {
        errorOccurred = true;
        if (stopOnError) {
            stop = true;
        }
        if (errorHandlingPolicy == null) {
            super.handle(e);
        } else {
            errorHandlingPolicy.handle(e);
        }
    }


    @Override
    public void setErrorHandler(ErrorHandlingPolicy handler) {
        this.errorHandlingPolicy = handler;
    }

    @Override
    public EventEx<Object> getOnRobotInterrupt() {
        return parent.getOnRobotInterrupt();
    }

    /**
     * @return whether an error occurred
     */
    public boolean hasErrorOccurred() {
        return errorOccurred;
    }

    /**
     * Sets whether the debugger should stop if an error occurs.
     *
     * @param stopOnError whether the robot will stop when an error occurs
     */
    public void setStopOnError(boolean stopOnError) {
        this.stopOnError = stopOnError;
    }

    @Override
    public List<Instruction> getStackTrace() {
        return currentStack;
    }
}
