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

import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.*;
import nl.xillio.xill.api.errors.NotImplementedException;
import nl.xillio.xill.api.errors.RobotConcurrentModificationException;

import java.util.*;
import java.util.function.Supplier;

/**
 * This {@link Instruction} represents the foreach looping context.
 */
public class ForeachInstruction extends CompoundInstruction {
    private final InstructionSet instructionSet;
    private final Processable list;
    private final VariableDeclaration valueVar;
    private final VariableDeclaration keyVar;

    /**
     * Create a {@link ForeachInstruction} with key and value variables
     *
     * @param instructionSet the instructionSet to run
     * @param list           the list of values
     * @param valueVar       the reference to the value variable
     * @param keyVar         the reference to the key variable
     */
    public ForeachInstruction(final InstructionSet instructionSet, final Processable list, final VariableDeclaration valueVar, final VariableDeclaration keyVar) {
        this.instructionSet = instructionSet;
        instructionSet.setParentInstruction(this);

        this.list = list;
        this.valueVar = valueVar;
        this.keyVar = keyVar;

        valueVar.setHostInstruction(instructionSet);
        if (keyVar != null) {
            keyVar.setHostInstruction(instructionSet);
        }
    }

    /**
     * Create a {@link ForeachInstruction} without a key variable
     *
     * @param instructionSet the instructionSet to run
     * @param list           the list of values
     * @param valueVar       the reference to the value variable
     */
    public ForeachInstruction(final InstructionSet instructionSet, final Processable list, final VariableDeclaration valueVar) {
        this(instructionSet, list, valueVar, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) {
        // Create the list instruction, set the host instruction and position.
        ExpressionInstruction listInstruction = new ExpressionInstruction(list);
        listInstruction.setHostInstruction(getHostInstruction());
        listInstruction.setPosition(getPosition());

        // Start, process, and end the instruction.
        debugger.startInstruction(listInstruction);
        if (debugger.shouldStop()) {
            debugger.endInstruction(listInstruction, InstructionFlow.doReturn(ExpressionBuilderHelper.NULL));
            return InstructionFlow.doReturn(ExpressionBuilderHelper.NULL);
        }
        InstructionFlow<MetaExpression> flow = listInstruction.process(debugger);
        debugger.endInstruction(listInstruction, flow);

        MetaExpression result = flow.get();

        // Register a reference to the result and process it.
        try {
            return process(result, debugger);
        } finally {
            listInstruction.close();
        }
    }

    private InstructionFlow<MetaExpression> process(MetaExpression result, Debugger debugger) {
        // Check if the input is null.
        if (result.isNull()) {
            return InstructionFlow.doResume();
        }

        switch (result.getType()) {
            case ATOMIC:
                return iterateAtomic(debugger, result);
            case LIST:
                return tryIterations(debugger, ((List<MetaExpression>) result.getValue()).iterator(), null);
            case OBJECT:
                Map<String, MetaExpression> map = result.getValue();
                return tryIterations(debugger, map.values().iterator(), map.keySet());
            default:
                throw new NotImplementedException("This type has not been implemented."); // Should never happen.
        }
    }

    private InstructionFlow<MetaExpression> iterateAtomic(Debugger debugger, MetaExpression value) {
        // If the atomic has an iterable meta, iterate over that. Otherwise do a single iteration.
        if (value.hasMeta(MetaExpressionIterator.class)) {
            return doIterations(debugger, value.getMeta(MetaExpressionIterator.class), null);
        } else {
            return doIterations(debugger, new SingletonIterator<>(value), null);
        }
    }

    private InstructionFlow<MetaExpression> tryIterations(Debugger debugger, Iterator<MetaExpression> valueIterable, Set<String> keySet) {
        // Try to do the iterations, catching exceptions that will occur when modifying the collection.
        try {
            return doIterations(debugger, valueIterable, keySet);
        } catch (ConcurrentModificationException e) {
            throw new RobotConcurrentModificationException(e);
        }
    }

    //squid:S135 Two breaks keep the code readable, while adding code in between if blocks is not possible here
    //squid:S2095 suppress 'close this MetaExpression': The metaExpressions do not have to be closed here. They are closed somewhere else later.
    @SuppressWarnings({"squid:S135", "squid:S2095"})
    private InstructionFlow<MetaExpression> doIterations(Debugger debugger, Iterator<MetaExpression> valueIterator, Set<String> keySet) {
        InstructionFlow<MetaExpression> result = InstructionFlow.doResume();
        int index = 0;
        Iterator<String> keys = keySet != null ? keySet.iterator() : null;

        // Iterate over all values.
        while (valueIterator.hasNext()) {
            MetaExpression value = valueIterator.next();

            // If there are string keys, get the next one.
            String keyString = keys != null ? keys.next() : null;

            // Get the next key as a meta expression.
            MetaExpression key = keyString != null ? ExpressionBuilderHelper.fromValue(keyString) : ExpressionBuilderHelper.fromValue(index);

            InstructionFlow<MetaExpression> instructionResult = processIteration(() -> key, value, debugger);

            // If the instruction returns or breaks, set the result and break out of the loop.
            if (instructionResult.returns()) {
                result = instructionResult;
                break;
            } else if (instructionResult.breaks()) {
                result = InstructionFlow.doResume();
                break;
            }

            // Increase the index.
            index++;
        }

        return result;
    }

    private InstructionFlow<MetaExpression> processIteration(Supplier<MetaExpression> key, MetaExpression value, Debugger debugger) {
        // Push the value and key variable values.
        valueVar.pushVariable(value, debugger.getStackDepth());
        if (keyVar != null) {
            keyVar.pushVariable(key.get(), debugger.getStackDepth());
        }

        InstructionFlow<MetaExpression> instructionResult = instructionSet.process(debugger);

        if (instructionResult.returns() && instructionResult.hasValue()) {
            // Prevent the instruction result from being disposed.
            instructionResult.get().preventDisposal();
            releaseVariables();
            instructionResult.get().allowDisposal();
        } else {
            releaseVariables();
        }

        return instructionResult;
    }

    private void releaseVariables() {
        // Release the value and key variables.
        valueVar.releaseVariable();
        if (keyVar != null) {
            keyVar.releaseVariable();
        }
    }

    @Override
    public Collection<Processable> getChildren() {
        if (keyVar != null) {
            return Arrays.asList(valueVar, keyVar, list, instructionSet);
        }
        return Arrays.asList(valueVar, list, instructionSet);
    }


    /**
     * Implementation of an iterator that takes a single object which it will iterate over once
     *
     * @param <E> The object to iterate once
     */
    static class SingletonIterator<E> implements Iterator<E> {
        private final E item;
        private boolean gotItem = false;

        SingletonIterator(final E item) {
            this.item = item;
        }

        @Override
        public boolean hasNext() {
            return !this.gotItem;
        }

        @Override
        public E next() {
            if (this.gotItem) {
                throw new NoSuchElementException();
            }
            this.gotItem = true;
            return item;
        }

        @Override
        public void remove() {
            if (!this.gotItem) {
                this.gotItem = true;
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
