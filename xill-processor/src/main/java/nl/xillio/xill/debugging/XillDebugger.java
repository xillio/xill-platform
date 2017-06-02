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

import me.biesaart.utils.Log;
import nl.xillio.events.Event;
import nl.xillio.events.EventHost;
import nl.xillio.xill.api.*;
import nl.xillio.xill.api.components.*;
import nl.xillio.xill.api.errors.ErrorHandlingPolicy;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.events.RobotContinuedAction;
import nl.xillio.xill.api.events.RobotPausedAction;
import nl.xillio.xill.api.events.RobotStartedAction;
import nl.xillio.xill.api.events.RobotStoppedAction;
import nl.xillio.xill.components.instructions.CompoundInstruction;
import nl.xillio.xill.components.instructions.FunctionDeclaration;
import nl.xillio.xill.components.instructions.Instruction;
import nl.xillio.xill.components.instructions.VariableDeclaration;
import org.slf4j.Logger;
import org.slf4j.MarkerFactory;
import xill.lang.xill.Target;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains all information and controls required for debugging.
 *
 * @author Thomas Biesaart
 */
public class XillDebugger implements Debugger {
    private static final Logger LOGGER = Log.get();
    private final List<Breakpoint> breakpoints;
    private final EventHost<RobotStartedAction> onRobotStarted = new EventHost<>();
    private final EventHost<RobotStoppedAction> onRobotStopped = new EventHost<>();
    private final EventHost<RobotPausedAction> onRobotPaused = new EventHost<>();
    private final EventHost<RobotContinuedAction> onRobotContinued = new EventHost<>();
    private final EventHostEx<Object> onRobotInterrupt = new EventHostEx<>();
    private final Stack<nl.xillio.xill.api.components.Instruction> currentStack = new Stack<>();
    private final Stack<CounterWrapper> functionStack = new Stack<>();
    private final LinkedList<Debugger> childDebuggers = new LinkedList<>();
    private DebugInfo debugInfo = new DebugInfo();
    private Instruction pausedOnInstruction = null;
    private ErrorHandlingPolicy handler = new NullDebugger();
    private Mode mode = Mode.RUNNING;
    private OutputHandler outputHandler;


    /**
     * Create a new {@link XillDebugger}.
     */
    public XillDebugger() {
        breakpoints = new ArrayList<>();
    }

    @Override
    public void pause(boolean userAction) {
        mode = Mode.PAUSED;
        childDebuggers.forEach(dbg -> dbg.pause(userAction));
    }

    @Override
    public void stop() {
        mode = Mode.STOPPED;
        onRobotInterrupt.invoke(null);
        childDebuggers.forEach(Debugger::stop);
    }

    @Override
    public void resume() {
        mode = Mode.RUNNING;
    }

    @Override
    public void stepIn() {
        resume();
        mode = Mode.STEP_IN;
    }

    @Override
    public void stepOver() {
        resume();
        mode = Mode.STEP_OVER;
    }

    @Override
    public void startInstruction(final nl.xillio.xill.api.components.Instruction instruction) {
        Instruction internalInstruction = (Instruction) instruction;
        currentStack.add(internalInstruction);
        checkBreakpoints(internalInstruction);
        checkStepIn();
        checkPause(internalInstruction);
    }

    private void checkStepIn() {
        if (mode == Mode.STEP_IN) {
            mode = Mode.PAUSED;
        }
    }

    private void checkBreakpoints(Instruction instruction) {
        if (mode != Mode.STOPPED) {
            breakpoints.stream()
                    .filter(bp -> bp.matches(instruction))
                    .findAny()
                    .ifPresent(bp -> this.mode = Mode.PAUSED);
        }
    }

    @Override
    public void endInstruction(final nl.xillio.xill.api.components.Instruction instruction, final InstructionFlow<MetaExpression> result) {
        Instruction internalInstruction = (Instruction) instruction;
        checkPause(internalInstruction);
        checkStepOver(internalInstruction);
        currentStack.pop();
    }

    private void checkStepOver(Instruction instruction) {
        if (mode == Mode.STEP_OVER) {
            if (isCompoundInstruction(pausedOnInstruction)) {
                mode = Mode.PAUSED;
            } else if (instruction == pausedOnInstruction) {
                mode = Mode.PAUSED;
            }
        }
    }

    /**
     * Check if this instruction is a compound instruction.
     *
     * @param instruction the instruction to check
     * @return true if and only if the instruction is a compound instruction AND not a FunctionCall
     */
    private boolean isCompoundInstruction(Instruction instruction) {
        return instruction instanceof CompoundInstruction;
    }

    /**
     * Check if the robot should pause.
     *
     * @param instruction The instruction to pause on (i.e. pass to the pause event)
     */
    private void checkPause(final Instruction instruction) {
        if (mode == Mode.PAUSED) {
            pausedOnInstruction = instruction;
            onRobotPaused.invoke(new RobotPausedAction(instruction));
            while (mode == Mode.PAUSED) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    LOGGER.error("Interrupted while sleeping", e);
                }
            }
            onRobotContinued.invoke(new RobotContinuedAction(instruction));
        }
    }

    @Override
    public void returning(final InstructionSet instructionSet, final InstructionFlow<MetaExpression> result) {
    }

    /**
     * Add breakpoint to this debugger.
     *
     * @param breakpoint the breakpoint
     */
    @Override
    public void addBreakpoint(final Breakpoint breakpoint) {
        breakpoints.add(breakpoint);
    }

    /**
     * Remove all breakpoints from this debugger.
     */
    public void clearBreakpoints() {
        breakpoints.clear();
    }

    @Override
    public void robotStarted(final Robot robot) {
        onRobotInterrupt.getListeners().clear();
        onRobotStarted.invoke(new RobotStartedAction(robot));
        resume();
        currentStack.clear();
        functionStack.clear();
    }

    @Override
    public void robotFinished(final Robot robot) {
        onRobotStopped.invoke(new RobotStoppedAction(robot, robot.getCompilerSerialId()));

        LOGGER.info(MarkerFactory.getMarker("ROBOT_END"), "Robot (CSID: " + robot.getCompilerSerialId() + ") finished.");
    }

    @Override
    public void addDebugInfo(final nl.xillio.xill.api.DebugInfo info) {
        debugInfo.add((DebugInfo) info);
    }

    @Override
    public Event<RobotStartedAction> getOnRobotStart() {
        return onRobotStarted.getEvent();
    }

    @Override
    public Event<RobotStoppedAction> getOnRobotStop() {
        return onRobotStopped.getEvent();
    }

    @Override
    public Event<RobotPausedAction> getOnRobotPause() {
        return onRobotPaused.getEvent();
    }

    @Override
    public Event<RobotContinuedAction> getOnRobotContinue() {
        return onRobotContinued.getEvent();
    }

    @Override
    public EventEx<Object> getOnRobotInterrupt() {
        return onRobotInterrupt.getEvent();
    }

    @Override
    public void setBreakpoints(final List<Breakpoint> breakpoints) {
        clearBreakpoints();
        this.breakpoints.addAll(breakpoints);
    }

    @Override
    public boolean shouldStop() {
        return mode == Mode.STOPPED;
    }

    @SuppressWarnings("squid:S1166") // Ignored exception is correct here
    @Override
    public Collection<Object> getVariables(nl.xillio.xill.api.components.Instruction instruction) {
        if (mode != Mode.PAUSED) {
            throw new IllegalStateException("Cannot get variables if not paused.");
        }

        if (instruction == null) {
            // No instruction == no results
            return Collections.emptyList();
        }

        nl.xillio.xill.components.instructions.InstructionSet instructionSet = ((nl.xillio.xill.components.instructions.Instruction) instruction).getHostInstruction();

        if (instructionSet == null) {
            LOGGER.warn("No instruction set found for " + instruction);
            return Collections.emptyList();
        }

        return getVariableTokens((Instruction) instruction);
    }

    private List<Object> getVariableTokens(Instruction instruction) {
        List<ScopeCheckResult> variables = debugInfo.getVariables()
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .filter(dec -> isVisible(dec, instruction))
                .map(dec -> checkScope(dec, instruction, 0))
                .filter(ScopeCheckResult::isInScope)
                .collect(Collectors.toList());

        Map<String, ScopeCheckResult> result = new HashMap<>();

        for (ScopeCheckResult dec : variables) {
            String name = dec.getDeclaration().getName();
            if (!result.containsKey(name)) {
                result.put(name, dec);
            } else if (result.get(name).getScopeDepth() > dec.getScopeDepth()) {
                result.put(name, dec);
            }
        }

        return result.values()
                .stream()
                .map(ScopeCheckResult::getDeclaration)
                .sorted((a, b) -> Integer.compare(a.getLineNumber(), b.getLineNumber()))
                .map(debugInfo::getTarget)
                .collect(Collectors.toList());
    }

    private boolean isVisible(VariableDeclaration variableDeclaration, Instruction instruction) {
        return variableDeclaration.getRobotID() == instruction.getRobotID();
    }

    private ScopeCheckResult checkScope(VariableDeclaration variableDeclaration, Instruction checkInstruction, int checkDepth) {
        if (variableDeclaration.getHostInstruction() == checkInstruction.getHostInstruction()) {
            return new ScopeCheckResult(checkDepth, variableDeclaration.hasValue() && checkInstruction.getLineNumber() > variableDeclaration.getLineNumber(), variableDeclaration);
        }

        nl.xillio.xill.components.instructions.InstructionSet instructionSet = checkInstruction.getHostInstruction();
        Instruction parentInstruction = instructionSet.getParentInstruction();

        // We have no parent... move on
        if (parentInstruction == null) {
            return new ScopeCheckResult(checkDepth, false, variableDeclaration);
        }

        return checkScope(variableDeclaration, parentInstruction, checkDepth + 1);
    }

    @Override
    public MetaExpression getVariableValue(final Object identifier, int stackPosition) {
        VariableDeclaration dec = debugInfo.getVariables().get(identifier);

        // position counting from the bottom of the stack starting at 0
        int bottomPosition = getStackDepth() - (stackPosition - 1);

        // Go through all scopes of the current function to find a value on the stack
        // Stop at the function scope to prevent variables assigned in a previous recursive scope to be returned
        MetaExpression value;
        Instruction parent = dec;
        do {
            value = dec.peek(bottomPosition);
            parent = parent.getHostInstruction().getParentInstruction();
            bottomPosition--;
        }
        while (value == null && parent != null && !(parent instanceof FunctionDeclaration));

        // If the variable was not found in a function call, look for it at robot level
        if (value == null) {
            value = dec.peek(0);
        }

        return value;
    }

    private FunctionDeclaration getParentFunction(Instruction instruction) {
        nl.xillio.xill.components.instructions.InstructionSet set = instruction.getHostInstruction();
        Instruction parentInstruction = set.getParentInstruction();

        if (parentInstruction == null) {
            // No parent instruction found for instruction
            return null;
        }

        if (parentInstruction instanceof FunctionDeclaration) {
            return (FunctionDeclaration) parentInstruction;
        }

        return getParentFunction(parentInstruction);
    }

    @Override
    public String getVariableName(final Object identifier) {
        if (!(identifier instanceof Target)) {
            return null;
        }

        return ((Target) identifier).getName();
    }

    @Override
    public void reset() {
        if (mode == Mode.STOPPED) {
            debugInfo = new DebugInfo();
        }
    }

    @Override
    public void handle(final Throwable e) throws RobotRuntimeException {
        if (outputHandler != null) {
            nl.xillio.xill.api.components.Instruction instruction = getStackTrace().stream().findFirst().orElse(null);
            outputHandler.inspect(instruction, e);
        }
        handler.handle(e);
    }

    @Override
    public void setErrorHandler(final ErrorHandlingPolicy handler) {
        this.handler = handler;
    }

    @Override
    public void setOutputHandler(OutputHandler handler) {
        this.outputHandler = handler;
    }

    @Override
    public List<nl.xillio.xill.api.components.Instruction> getStackTrace() {
        return currentStack;
    }

    @Override
    public int getStackDepth() {
        int stackSize = currentStack.size();
        // The stack size is 0 for variable initializers in included robots, those should be on depth 0 too
        return stackSize > 0 ? stackSize - 1 : 0;
    }

    @Override
    public Debugger createChild() {
        Debugger debugger = new StoppableDebugger(this);
        debugger.setOutputHandler(outputHandler);
        childDebuggers.add(debugger);
        return debugger;
    }

    @Override
    public void removeChild(final Debugger debugger) {
        childDebuggers.remove(debugger);
    }

    @Override
    public void startFunction(Processable functionDeclaration) {
        functionStack.push(new CounterWrapper(functionDeclaration, currentStack.size()));
    }

    @Override
    public void endFunction(Processable functionDeclaration) {
        functionStack.pop();
    }

    private enum Mode {
        RUNNING,
        STEP_IN,
        STEP_OVER,
        PAUSED,
        STOPPED
    }

    private class CounterWrapper {
        private final Processable processable;
        private final int stackSize;

        private CounterWrapper(Processable processable, int stackSize) {
            this.processable = processable;
            this.stackSize = stackSize;
        }

        public Processable getProcessable() {
            return processable;
        }

        public int getStackSize() {
            return stackSize;
        }
    }

    private class ScopeCheckResult {
        private final int scopeDepth;
        private final boolean inScope;
        private final VariableDeclaration declaration;

        private ScopeCheckResult(int scopeDepth, boolean inScope, VariableDeclaration declaration) {
            this.scopeDepth = scopeDepth;
            this.inScope = inScope;
            this.declaration = declaration;
        }

        public int getScopeDepth() {
            return scopeDepth;
        }

        public boolean isInScope() {
            return inScope;
        }

        public VariableDeclaration getDeclaration() {
            return declaration;
        }
    }
}
