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
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Processable;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.util.Arrays;
import java.util.Collection;

import static nl.xillio.xill.api.components.ExpressionBuilder.fromValue;

/**
 * This class represents the &amp;&amp; operator.
 */
public final class And implements Processable {

    private final Processable left;
    private final Processable right;

    /**
     * The constructor to create a new {@link And}-object.
     *
     * @param left     The left-hand side of the expression.
     * @param right    The right-hand side of the expression.
     */
    public And(final Processable left, final Processable right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) throws RobotRuntimeException {
        boolean result;

        MetaExpression leftValue = left.process(debugger).get();
        leftValue.registerReference();
        result = leftValue.getBooleanValue();
        leftValue.releaseReference();

        if (result) {
            MetaExpression rightValue = right.process(debugger).get();
            rightValue.registerReference();
            result = rightValue.getBooleanValue();
            rightValue.releaseReference();
        }

        return InstructionFlow.doResume(fromValue(result));
    }


    @Override
    public Collection<Processable> getChildren() {
        return Arrays.asList(left, right);
    }

}
