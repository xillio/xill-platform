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

import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.ExpressionBuilder;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Processable;
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.components.instructions.VariableDeclaration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * This class represents the + operation.
 */
public final class IntegerShortcut implements Processable {

    private final Assign assign;
    private final boolean returnFirst;
    private final int additiveValue;

    /**
     * Create a new {@link IntegerShortcut}-object.
     *
     * @param variable      The result of the addition.
     * @param path          The path to assign to.
     * @param value         The first value in the expression.
     * @param additiveValue The second value in the expression.
     * @param returnFirst   true for suffix mode, false for prefix
     */
    public IntegerShortcut(final VariableDeclaration variable, final List<Processable> path, final Processable value, final int additiveValue, final boolean returnFirst) {
        this.additiveValue = additiveValue;
        this.returnFirst = returnFirst;
        assign = new Assign(variable, path, new Add(value, new ExpressionBuilder(additiveValue)));
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) throws RobotRuntimeException {
        // Actually assign the value
        MetaExpression assignedValue = assign.processWithValue(debugger);
        assignedValue.registerReference();

        // Now we need to determine the return type
        long value = assignedValue.getNumberValue().longValue();
        assignedValue.releaseReference();

        if (returnFirst) {
            // This is suffix mode so revert the addition
            value -= additiveValue;
        }

        return InstructionFlow.doResume(ExpressionBuilderHelper.fromValue(value));
    }

    @Override
    public Collection<Processable> getChildren() {
        return Arrays.asList(assign);
    }
}
