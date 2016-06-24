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

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;

/**
 * This class represents the || operator
 */
public class Negate implements Processable {

    private final Processable value;

    /**
     * Create a new {@link Negate}-object.
     *
     * @param value    The value to be negated.
     */
    public Negate(final Processable value) {
        this.value = value;

    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) throws RobotRuntimeException {
        MetaExpression processedValue = value.process(debugger).get();
        processedValue.registerReference();

        boolean result = !processedValue.getBooleanValue();

        processedValue.releaseReference();
        return InstructionFlow.doResume(fromValue(result));
    }


    @Override
    public Collection<Processable> getChildren() {
        return Arrays.asList(value);
    }

}
