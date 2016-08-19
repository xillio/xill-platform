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
import nl.xillio.xill.api.events.RobotContinuedAction;
import nl.xillio.xill.api.events.RobotPausedAction;
import nl.xillio.xill.api.events.RobotStartedAction;
import nl.xillio.xill.api.events.RobotStoppedAction;

import java.util.Collection;
import java.util.List;

/**
 * This interface represents the container for all debugging information
 */
public interface Debugger extends ErrorHandlingPolicy {

    // BREAKPOINTS

    /**
     * Pauses the robot.
     *
     * @param userAction {@code true} when the pause has been triggered by the user, {@code false} if the reason was an error or warning
     */
    void pause(boolean userAction);

    /**
     * Stops and kills the current robot.
     */
    void stop();

    /**
     * Resumes the robot execution.
     */
    void resume();

    /**
     * Steps into the current instruction.
     */
    void stepIn();

    /**
     * Steps over to the next instruction.
     */
    void stepOver();

    /**
     * Adds a new breakpoint.
     *
     * @param breakpoint the breakpoint to add
     */
    void addBreakpoint(final Breakpoint breakpoint);

    /**
     * Replaces the breakpoints in this debugger with the provided list.
     *
     * @param breakpoints the list of breakpoints to set
     */
    void setBreakpoints(final List<Breakpoint> breakpoints);

    /**
     * This method is called before processing an instruction.
     *
     * @param instruction the instruction that started
     */
    void startInstruction(final Instruction instruction);

    /**
     * This method is called after an instruction has been processed.
     *
     * @param instruction the instruction that ended
     * @param result      the result of the instruction
     */
    void endInstruction(final Instruction instruction, final InstructionFlow<MetaExpression> result);

    /**
     * @param instructionSet the set that is returning a value
     * @param result         the returned value
     */
    void returning(final InstructionSet instructionSet, final InstructionFlow<MetaExpression> result);

    /**
     * This method is called whenever a robot starts.
     *
     * @param robot the robot that started
     */
    void robotStarted(final Robot robot);

    /**
     * This method is called whenever a robot ends.
     *
     * @param robot the robot that finished
     */
    void robotFinished(final Robot robot);

    /**
     * Adds debugging info.
     *
     * @param info the info that should be added
     */
    void addDebugInfo(final DebugInfo info);

    // EVENTS

    /**
     * @return The Event
     */
    Event<RobotStartedAction> getOnRobotStart();

    /**
     * @return The Event
     */
    Event<RobotStoppedAction> getOnRobotStop();

    /**
     * @return The Event
     */
    Event<RobotPausedAction> getOnRobotPause();

    /**
     * @return The Event
     */
    Event<RobotContinuedAction> getOnRobotContinue();

    /**
     * @return The Event
     */
    EventEx<Object> getOnRobotInterrupt();

    /**
     * @return whether the robot should be killed
     */
    boolean shouldStop();

    // VARIABLE DEBUGGING

    /**
     * Returns a list of variable identifiers from the perspective (scope) of an instruction.
     *
     * @param instruction the instruction
     * @return the variables
     */
    Collection<Object> getVariables(Instruction instruction);

    /**
     * Gets the value of a variable, keeping recursivity in mind.
     *
     * @param identifier    the identifier returned by {@link Debugger#getVariables(Instruction)}
     * @param stackPosition The count of the stack frame to start looking for (top down), counting from the top instruction (starting at 1)
     * @return The current value in a variable
     * @see Debugger#getVariables(Instruction)
     */
    MetaExpression getVariableValue(final Object identifier, int stackPosition);

    /**
     * @param identifier the identifier returned by {@link Debugger#getVariables(Instruction)}
     * @return The name of a variable
     * @see Debugger#getVariables(Instruction)
     */
    String getVariableName(final Object identifier);

    /**
     * Purges all information.
     */
    void reset();

    /**
     * @param handler the handler to set
     */
    void setErrorHandler(final ErrorHandlingPolicy handler);

    /**
     * @return the stack trace to the current instruction
     */
    List<Instruction> getStackTrace();

    /**
     * @return The index of the last frame on the stack
     */
    int getStackDepth();

    /**
     * Instantiates a child debugger.
     *
     * @return the debugger
     */
    Debugger createChild();

    /**
     * Removes the child debugger's internal reference.
     * It should be called when the child debugger created by createChild is no longer needed.
     *
     * @param debugger the child debugger
     */
    void removeChild(final Debugger debugger);

    /**
     * Starts processing a function call.
     *
     * @param functionDeclaration the function
     */
    void startFunction(Processable functionDeclaration);

    /**
     * Ends the function.
     *
     * @param functionDeclaration the function
     */
    void endFunction(Processable functionDeclaration);

    /**
     * Getter for ProgressInfo event
     *
     * @return the event
     */
    Event<ProgressInfo> getOnSetProgressInfo();

    /**
     * Set current progress status of the robot
     *
     * @param progressInfo the info about current robot's progress; serves here for data transferring only
     */
    void setProgressInfo(ProgressInfo progressInfo);
}
