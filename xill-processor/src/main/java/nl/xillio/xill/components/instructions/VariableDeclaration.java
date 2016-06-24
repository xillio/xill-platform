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
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.*;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.components.operators.Assign;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Stack;

/**
 * This {@link Instruction} represents the start of a variable's lifespan.
 * <p>
 * A VariableDeclaration can run in one of two modes: var or argument.
 * In var mode the value of this variable will be
 * set to the value after the assignment operator (or null).
 * In argument mode the value of this variable will be set to the argument passed to this robot, or use the expression
 * value as a fallback.
 *
 * @author Thomas biesaart
 */
public class VariableDeclaration extends Instruction {
    private final Processable assignation;
    // A stack of stack position, value pairs
    private final Stack<Pair<Integer, MetaExpression>> valueStack = new Stack<>();
    /**
     * This is here for debugging purposes.
     */
    private final String name;

    /**
     * Create a new {@link VariableDeclaration}.
     *
     * @param expression the default value expression
     * @param name       the name of this declaration for debugging purposes
     * @param robot      if this declaration is an 'argument' then the robot that holds the argument
     */
    public VariableDeclaration(final Processable expression, final String name, Robot robot) {
        this.name = name;
        Processable selectedExpression = robot == null ? expression : new ArgumentProvider(expression, robot);
        assignation = new Assign(this, Collections.emptyList(), selectedExpression);
    }

    public VariableDeclaration(Processable expression, String name) {
        this(expression, name, null);
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) {
        pushVariable(ExpressionBuilderHelper.NULL, getInsertionIndex(debugger));
        assignation.process(debugger);

        return InstructionFlow.doResume();
    }

    /**
     * Get the index to insert a value at in the value stack
     * @param debugger The current debugger
     * @return The index
     */
    protected int getInsertionIndex(Debugger debugger) {
        return debugger.getStackDepth();
    }

    /**
     * @return the expression of the variable or null
     */
    public MetaExpression getVariable() {
        if (!valueStack.isEmpty()) {
            return valueStack.peek().getValue();
        }
        return ExpressionBuilder.NULL;
    }

    /**
     * Set the value of the variable.
     *
     * @param value The value to which the variable needs to be set.
     */
    public void replaceVariable(final MetaExpression value) {
        if (hasValue()) {
            Pair<Integer, MetaExpression> replace = valueStack.pop();
            MetaExpression current = replace.getValue();
            pushVariable(value, replace.getKey());
            current.releaseReference();
        } else {
            throw new RobotRuntimeException("Reference to unknown variable '" + getName() + "', could not assign value.");
        }
    }

    /**
     * Set the value of the variable without popping the last one
     *
     * @param value         The value of the variable.
     * @param stackPosition The position the variable is in counting from the robot root, starting at 0
     */
    public void pushVariable(final MetaExpression value, final int stackPosition) {
        value.registerReference();
        valueStack.push(new ImmutablePair<>(stackPosition, value));
    }

    /**
     * Release the current variable.
     */
    public void releaseVariable() {
        valueStack.pop().getValue().releaseReference();
    }

    /**
     * A variable declared to be null.
     *
     * @param position The position in code where the null variable occurs.
     * @param name     The name of the variable that is declared to be null.
     * @return A declaration with value {@link ExpressionBuilder#NULL}
     */
    public static VariableDeclaration nullDeclaration(final CodePosition position, final String name) {
        VariableDeclaration dec = new VariableDeclaration(ExpressionBuilderHelper.NULL, name);
        dec.setPosition(position);

        return dec;
    }

    @Override
    public Collection<Processable> getChildren() {
        return Arrays.asList(assignation);
    }

    @Override
    public void close() throws Exception {
        releaseVariable();
    }

    /**
     * This name is for debugging purposes and is <b>NOT UNIQUE</b>.
     * Do not use as identifier
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Check if there is a value at any position in the stack
     * @return True if a value exists, false if not
     */
    public boolean hasValue() {
        return !valueStack.empty();
    }

    /**
     * Peek at the value at the given stack position
     *
     * @param stackPosition The position in the stack counting from the root of the robot, starting at 0
     * @return The variable at the stack position, or null of it does not exist
     */
    public MetaExpression peek(int stackPosition) {
        for (Pair<Integer, MetaExpression> stackElement : valueStack) {
            // Already past the stack position
            int position = stackElement.getKey();
            if (position > stackPosition) {
                return null;
            }
            if (position == stackPosition) {
                return stackElement.getValue();
            }
        }
        return null;
    }

    /**
     * This class represents an expression that has a variable source. It is used to make argument declarations possible.
     *
     * @author Thomas biesaart
     */
    private class ArgumentProvider implements Processable {

        private final Processable expression;
        private final Robot robot;

        public ArgumentProvider(Processable expression, Robot robot) {
            this.expression = expression;
            this.robot = robot;
        }

        @Override
        public InstructionFlow<MetaExpression> process(Debugger debugger) {
            if (robot.hasArgument()) {
                return InstructionFlow.doResume(robot.getArgument());
            }

            return InstructionFlow.doResume(expression.process(debugger).get());
        }

        @Override
        public Collection<Processable> getChildren() {
            return Collections.singletonList(expression);
        }
    }
}
