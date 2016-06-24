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
package nl.xillio.xill.components;

import com.google.common.collect.Lists;
import me.biesaart.utils.Log;
import nl.xillio.events.EventHost;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.*;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.events.RobotStartedAction;
import nl.xillio.xill.api.events.RobotStoppedAction;
import nl.xillio.xill.components.instructions.FunctionDeclaration;
import nl.xillio.xill.components.instructions.InstructionSet;
import nl.xillio.xill.components.instructions.VariableDeclaration;
import org.slf4j.Logger;

import java.util.*;

/**
 * This class represents the root node of the program structure
 */
public class Robot extends InstructionSet implements nl.xillio.xill.api.components.Robot {
    private final RobotID robotID;
    private final List<Robot> libraries = new ArrayList<>();
    private MetaExpression callArgument;

    private List<Instruction> libraryProcessedInstructions = new ArrayList<>();
    private boolean initialized = false;
    private boolean closed = false;
    private boolean hasRun = false;

    private static final Logger LOGGER = Log.get();

    /**
     * Events for signalling that a robot has started or stopped
     */
    private EventHost<RobotStartedAction> startEvent;
    private EventHost<RobotStoppedAction> endEvent;

    private final UUID compilerSerialId;

    /**
     * Create a {@link Robot}-object.
     *
     * @param robotID          The ID of the robot.
     * @param debugger         The processor associated with the code in this robot.
     * @param startEvent       The event indicating the start of the execution of a robot.
     * @param endEvent         The event indicating the halting of a robot.
     * @param compilerSerialId Serial ID of the compiler.
     */
    public Robot(final RobotID robotID, final Debugger debugger, EventHost<RobotStartedAction> startEvent, EventHost<RobotStoppedAction> endEvent, UUID compilerSerialId) {
        super(debugger);
        this.robotID = robotID;
        this.startEvent = startEvent;
        this.endEvent = endEvent;
        this.compilerSerialId = compilerSerialId;
    }

    /**
     * Process this robot if necessary
     *
     * @return the result
     */
    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) throws RobotRuntimeException {
        if (hasRun) {
            // Cannot run a robot twice
            throw new RobotRuntimeException("This robot cannot run twice. It has to be re-initialized before running.");
        }
        getDebugger().robotStarted(this);
        startEvent.invoke(new RobotStartedAction(this));


        // Initialize all libraries and their children
        initializeAsLibrary(true);

        hasRun = true;
        InstructionFlow<MetaExpression> result = super.process(debugger);

        endEvent.invoke(new RobotStoppedAction(this, compilerSerialId));
        getDebugger().robotFinished(this);

        return result;
    }

    /**
     * @return the robotID
     */
    public RobotID getRobotID() {
        return robotID;
    }

    @Override
    public Collection<Processable> getChildren() {
        List<Processable> children = new ArrayList<>(super.getChildren());

        children.addAll(libraries);

        return children;
    }

    /**
     * Use a BFS algorithm to find a target among the children
     *
     * @param target The item to be found.
     * @return the path to the target or an empty list if the target wasn't
     * found.
     */
    @Override
    public List<Processable> pathToInstruction(final Instruction target) {
        Queue<Processable> fringe = new LinkedList<>();
        Set<Processable> visited = new HashSet<>();
        Map<Processable, Processable> parents = new HashMap<>();

        visited.add(this);
        fringe.add(this);
        while (!fringe.isEmpty()) {
            Processable currentItem = fringe.poll();
            visited.add(currentItem);

            if (currentItem == target) {
                // Found the target, let's make the list
                List<Processable> result = new ArrayList<>();

                while (currentItem != null) {
                    result.add(currentItem);
                    currentItem = parents.get(currentItem);
                }

                return Lists.reverse(result);
            }

            Processable parent = currentItem;
            // Seach children
            currentItem.getChildren().stream().filter(child -> !visited.contains(child)).forEach(child -> {
                visited.add(child);
                fringe.add(child);
                parents.put(child, parent);
            });
        }

        return new ArrayList<>();
    }

    @Override
    public void close() {
        super.close();

        // Close all libraries and their children
        closeAsLibrary();
    }

    /**
     * Add a library to this robot
     *
     * @param lib The library to be added.
     */
    public void addLibrary(final Robot lib) {
        libraries.add(lib);
    }

    @Override
    public void setArgument(final MetaExpression expression) {
        callArgument = expression;
    }

    @Override
    public MetaExpression getArgument() {
        if (callArgument == null) {
            return ExpressionBuilderHelper.NULL;
        }
        return callArgument;
    }

    @Override
    public boolean hasArgument() {
        return callArgument != null;
    }

    @Override
    public UUID getCompilerSerialId() {
        return compilerSerialId;
    }

    @Override
    public void initializeAsLibrary(boolean skipSelf) throws RobotRuntimeException {
        initialized = true;
        for (Robot robot : libraries) {
            if (!robot.initialized) {
                robot.initializeAsLibrary(false);
            }
        }
        if (!skipSelf) {
            for (nl.xillio.xill.components.instructions.Instruction instruction : getInstructions()) {
                if (getDebugger().shouldStop()) {
                    break;
                } else if ((instruction instanceof VariableDeclaration || instruction instanceof FunctionDeclaration)) {
                    instruction.process(getDebugger());
                    libraryProcessedInstructions.add(instruction);
                }
            }
        }
    }

    /**
     * Close variables and functions in an initialized library
     */
    public void closeAsLibrary() {
        closed = true;
        for (Robot robot : libraries) {
            if (!robot.closed) {
                robot.closeAsLibrary();
            }
        }
    }
}
