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

import nl.xillio.events.Event;
import nl.xillio.xill.api.components.*;
import nl.xillio.xill.api.errors.ErrorHandlingPolicy;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.events.RobotContinuedAction;
import nl.xillio.xill.api.events.RobotPausedAction;
import nl.xillio.xill.api.events.RobotStartedAction;
import nl.xillio.xill.api.events.RobotStoppedAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class represents a debugger that does absolutely nothing except forward exceptions to the output handler.
 *
 * @author Thomas Biesaart
 */
public class NullDebugger implements Debugger {
    private OutputHandler outputHandler;

    @Override
    public void stepIn() {
        // No Op
    }

    @Override
    public void stepOver() {
        // No Op
    }

    @Override
    public void startInstruction(final Instruction instruction) {
        // No Op
    }

    @Override
    public void endInstruction(final Instruction instruction, final InstructionFlow<MetaExpression> result) {
        // No Op
    }

    @Override
    public void returning(final InstructionSet instructionSet, final InstructionFlow<MetaExpression> result) {
        // No Op
    }

    @Override
    public void resume() {
        // No Op
    }

    @Override
    public void robotStarted(final Robot robot) {
        // No Op
    }

    @Override
    public void robotFinished(final Robot robot) {
        // No Op
    }

    @Override
    public void addDebugInfo(final DebugInfo info) {
        // No Op
    }

    @Override
    public Event<RobotStartedAction> getOnRobotStart() {
        return null;
    }

    @Override
    public Event<RobotStoppedAction> getOnRobotStop() {
        return null;
    }

    @Override
    public Event<RobotPausedAction> getOnRobotPause() {
        return null;
    }

    @Override
    public Event<RobotContinuedAction> getOnRobotContinue() {
        return null;
    }

    @Override
    public EventEx<Object> getOnRobotInterrupt() {
        return null;
    }

    @Override
    public void addBreakpoint(final Breakpoint breakpoint) {
        // No Op
    }

    @Override
    public void setBreakpoints(final List<Breakpoint> breakpoints) {
        // No Op
    }

    @Override
    public void pause(boolean userAction) {
        // No Op
    }

    @Override
    public void stop() {
        // No Op
    }

    @Override
    public boolean shouldStop() {
        return false;
    }

    @Override
    public Collection<Object> getVariables(Instruction instruction) {
        return new ArrayList<>();
    }

    @Override
    public MetaExpression getVariableValue(final Object identifier, int stackPosition) {
        return ExpressionBuilderHelper.fromValue(false);
    }

    @Override
    public String getVariableName(final Object identifier) {
        return "";
    }

    @Override
    public void reset() {
        // No Op
    }

    @Override
    public void handle(final Throwable e) throws RobotRuntimeException {
        sendToOutputHandler(e);
        if (e instanceof RobotRuntimeException) {
            throw (RobotRuntimeException) e;
        }
        throw new RobotRuntimeException("Exception in robot.", e);
    }

    protected void sendToOutputHandler(Throwable e) {
        if (outputHandler != null) {
            outputHandler.inspect(null, e);
        }
    }

    @Override
    public void setErrorHandler(final ErrorHandlingPolicy handler) {
        // No Op
    }

    @Override
    public void setOutputHandler(OutputHandler handler) {
        outputHandler = handler;
    }

    @Override
    public List<Instruction> getStackTrace() {
        return new ArrayList<>();
    }

    @Override
    public int getStackDepth() {
        return 0;
    }

    @Override
    public Debugger createChild() {
        return this;
    }

    @Override
    public void removeChild(final Debugger debugger) {
        // No Op
    }

    @Override
    public void startFunction(Processable functionDeclaration) {
        // No Op
    }

    @Override
    public void endFunction(Processable functionDeclaration) {
        // No Op
    }

    @Override
    public Event<nl.xillio.xill.api.ProgressInfo> getOnSetProgressInfo() {
        return null;
    }

    @Override
    public void setProgressInfo(ProgressInfo progressInfo) {
    }
}
