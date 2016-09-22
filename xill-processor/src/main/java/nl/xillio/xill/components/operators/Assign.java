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
package nl.xillio.xill.components.operators;

import com.google.common.collect.Lists;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Processable;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.components.instructions.Instruction;
import nl.xillio.xill.components.instructions.VariableDeclaration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This {@link Instruction} represents the assignment of an expression value to a variable.
 *
 * @author Thomas biesaart
 */
public class Assign implements Processable {

    private final VariableDeclaration variableDeclaration;
    private final Processable expression;
    private final List<Processable> path;

    /**
     * Create a new {@link Assign}.
     *
     * @param variableDeclaration The declaration to assign to
     * @param path                the path to assign to
     * @param expression          The expression to assign
     */
    public Assign(final VariableDeclaration variableDeclaration, final List<Processable> path, final Processable expression) {
        this.variableDeclaration = variableDeclaration;
        this.path = Lists.reverse(path);
        this.expression = expression;
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) {
        processWithValue(debugger);

        return InstructionFlow.doResume(ExpressionBuilderHelper.NULL);
    }

    // Assignment to list
    private void assign(final List<MetaExpression> target, final int pathID, final MetaExpression value, final Debugger debugger) {
        MetaExpression indexVal = path.get(pathID).process(debugger).get();
        indexVal.registerReference();
        int index = indexVal.getNumberValue().intValue();
        double indexD = indexVal.getNumberValue().doubleValue();
        indexVal.releaseReference();

        if (Double.isNaN(indexD)) {
            throw new RobotRuntimeException("A list cannot have named elements.");
        }
        if (!Double.isFinite(indexD)) {
            throw new RobotRuntimeException("A list cannot have infinite elements.");
        }
        if (path.size() - 1 == pathID) {
            // This is the value to write to
            if (index < 0) {
                throw new RobotRuntimeException("Cannot dereference negative array index.");
            }
            if (target.size() > index) {
                // Change the value and release reference of the old one
                target.set(index, value).releaseReference();
            } else {
                // The list is too small
                while (target.size() < index) {
                    target.add(ExpressionBuilderHelper.NULL);
                }

                target.add(value);
            }

            return;
        }

        // We need to go deeper
        MetaExpression currentValue = target.get(index).process(debugger).get();
        try {
            currentValue.registerReference();
            moveOn(currentValue, pathID, value, debugger);
        } finally {
            currentValue.releaseReference();
        }
    }

    private void assign(final Map<String, MetaExpression> target, final int pathID, final MetaExpression value, final Debugger debugger) {
        MetaExpression indexValue = path.get(pathID).process(debugger).get();
        indexValue.registerReference();
        String index = indexValue.getStringValue();
        indexValue.releaseReference();

        if (path.size() - 1 == pathID) {
            MetaExpression previous = target.put(index, value);
            if (previous != null) {
                previous.releaseReference();
            }
            return;
        }

        // We need to go deeper
        if (!target.containsKey(index)) {
            throw new RobotRuntimeException("Object '" + variableDeclaration.getName() + "' does not contain any element called '" + index + "'.");
        }

        // We need to go deeper
        MetaExpression currentValue = target.get(index).process(debugger).get();
        try {
            currentValue.registerReference();
            moveOn(currentValue, pathID, value, debugger);
        } finally {
            currentValue.releaseReference();
        }
    }

    @SuppressWarnings("unchecked")
    private void moveOn(MetaExpression currentValue, int pathID, MetaExpression value, Debugger debugger) {
        switch (currentValue.getType()) {
            case LIST:
                assign((List<MetaExpression>) currentValue.getValue(), pathID + 1, value, debugger);
                break;
            case OBJECT:
                assign((Map<String, MetaExpression>) currentValue.getValue(), pathID + 1, value, debugger);
                break;
            default:
                throw new IllegalStateException("Can only assign to children of OBJECT and LIST types.");
        }
    }

    @Override
    public Collection<Processable> getChildren() {
        // The variable declaration is not a child as it exists elsewhere

        List<Processable> children = new ArrayList<>(path);
        children.add(expression);

        return children;
    }

    /**
     * Process this {@link Assign} and return the assigned value
     *
     * @param debugger    The debugger object for processing the Assign-object.
     * @return            The assigned value
     */
    @SuppressWarnings("unchecked")
    public MetaExpression processWithValue(final Debugger debugger) {
        MetaExpression value = expression.process(debugger).get();

        // If the stop request or error occurred, interrupt the value assignment process (i.e. don't do any assignment)
        if(debugger.shouldStop()) {
            return ExpressionBuilderHelper.NULL;
        }

        // First we check if there is a path
        if (path.isEmpty()) {
            // Assign atomically
            variableDeclaration.replaceVariable(value);
        } else {
            // No root level assignment so we register the reference manually
            value.registerReference();

            // Seems like we have a path
            switch (variableDeclaration.getVariable().getType()) {
                case LIST:
                    List<MetaExpression> listValue = variableDeclaration.getVariable().getValue();
                    assign(listValue, 0, value, debugger);
                    break;
                case OBJECT:
                    Map<String, MetaExpression> mapValue = variableDeclaration.getVariable()
                            .getValue();
                    assign(mapValue, 0, value, debugger);
                    break;
                default:
                    throw new RobotRuntimeException("Cannot assign to atomic variable using a path.");
            }
        }

        return value;
    }

}
