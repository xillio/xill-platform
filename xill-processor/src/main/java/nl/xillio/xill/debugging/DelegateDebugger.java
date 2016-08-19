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
package nl.xillio.xill.debugging;

import nl.xillio.events.Event;
import nl.xillio.xill.api.*;
import nl.xillio.xill.api.DebugInfo;
import nl.xillio.xill.api.ProgressInfo;
import nl.xillio.xill.api.components.*;
import nl.xillio.xill.api.errors.ErrorHandlingPolicy;
import nl.xillio.xill.api.events.RobotContinuedAction;
import nl.xillio.xill.api.events.RobotPausedAction;
import nl.xillio.xill.api.events.RobotStartedAction;
import nl.xillio.xill.api.events.RobotStoppedAction;

import java.util.Collection;
import java.util.List;

/**
 * This class contains all information and controls required for debugging.
 *
 * @author Thomas Biesaart
 */
public class DelegateDebugger implements Debugger {
    private final Debugger debugger;

    public DelegateDebugger(Debugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public void pause(boolean isUser) {
        debugger.pause(isUser);
    }

    @Override
    public void stop() {
        debugger.stop();
    }

    @Override
    public void resume() {
        debugger.resume();
    }

    @Override
    public void stepIn() {
        debugger.stepIn();
    }

    @Override
    public void stepOver() {
        debugger.stepOver();
    }

    @Override
    public void addBreakpoint(Breakpoint breakpoint) {
        debugger.addBreakpoint(breakpoint);
    }

    @Override
    public void setBreakpoints(List<Breakpoint> breakpoints) {
        debugger.setBreakpoints(breakpoints);
    }

    @Override
    public void startInstruction(nl.xillio.xill.api.components.Instruction instruction) {
        debugger.startInstruction(instruction);
    }

    @Override
    public void endInstruction(nl.xillio.xill.api.components.Instruction instruction, InstructionFlow<MetaExpression> result) {
        debugger.endInstruction(instruction, result);
    }

    @Override
    public void returning(InstructionSet instructionSet, InstructionFlow<MetaExpression> result) {
        debugger.returning(instructionSet, result);
    }

    @Override
    public void robotStarted(Robot robot) {
        debugger.robotStarted(robot);
    }

    @Override
    public void robotFinished(Robot robot) {
        debugger.robotFinished(robot);
    }

    @Override
    public void addDebugInfo(DebugInfo info) {
        debugger.addDebugInfo(info);
    }

    @Override
    public Event<RobotStartedAction> getOnRobotStart() {
        return debugger.getOnRobotStart();
    }

    @Override
    public Event<RobotStoppedAction> getOnRobotStop() {
        return debugger.getOnRobotStop();
    }

    @Override
    public Event<RobotPausedAction> getOnRobotPause() {
        return debugger.getOnRobotPause();
    }

    @Override
    public Event<RobotContinuedAction> getOnRobotContinue() {
        return debugger.getOnRobotContinue();
    }

    @Override
    public EventEx<Object> getOnRobotInterrupt() {
        return debugger.getOnRobotInterrupt();
    }

    @Override
    public boolean shouldStop() {
        return debugger.shouldStop();
    }

    @Override
    public Collection<Object> getVariables(nl.xillio.xill.api.components.Instruction instruction) {
        return debugger.getVariables(instruction);
    }

    @Override
    public MetaExpression getVariableValue(Object identifier, int stackPosition) {
        return debugger.getVariableValue(identifier, stackPosition);
    }

    @Override
    public String getVariableName(Object identifier) {
        return debugger.getVariableName(identifier);
    }

    @Override
    public void reset() {
        debugger.reset();
    }

    @Override
    public void setErrorHandler(ErrorHandlingPolicy handler) {
        debugger.setErrorHandler(handler);
    }

    @Override
    public List<nl.xillio.xill.api.components.Instruction> getStackTrace() {
        return debugger.getStackTrace();
    }

    @Override
    public int getStackDepth() {
        return debugger.getStackDepth();
    }

    @Override
    public Debugger createChild() {
        return debugger.createChild();
    }

    @Override
    public void startFunction(Processable functionDeclaration) {
        debugger.startFunction(functionDeclaration);
    }

    @Override
    public void endFunction(Processable functionDeclaration) {
        debugger.endFunction(functionDeclaration);
    }

    @Override
    public Event<nl.xillio.xill.api.ProgressInfo> getOnSetProgressInfo() {
        return debugger.getOnSetProgressInfo();
    }

    @Override
    public void setProgressInfo(ProgressInfo progressInfo) {
        debugger.setProgressInfo(progressInfo);
    }

    @Override
    public void handle(Throwable e) {
        debugger.handle(e);
    }

    @Override
    public void removeChild(Debugger debug) {
        debugger.removeChild(debug);
    }
}
