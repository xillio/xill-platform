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
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Processable;

import java.util.Collection;
import java.util.Collections;

/**
 * This {@link Instruction} represents the end of a value holding scope.
 */
public class ReturnInstruction extends Instruction {

    private final Processable value;

    /**
     * Create a new {@link ReturnInstruction}.
     *
     * @param processable    Pass the object to be returned.
     */
    public ReturnInstruction(final Processable processable) {
        value = processable;
    }

    /**
     * Create a new null {@link ReturnInstruction}
     */
    public ReturnInstruction() {
        this(null);
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) {


        if (value == null) {
            return InstructionFlow.doReturn(ExpressionBuilderHelper.NULL);
        }

        InstructionFlow<MetaExpression> result = value.process(debugger);

        if (result.hasValue()) {
            return InstructionFlow.doReturn(result.get());
        }

        return InstructionFlow.doReturn(ExpressionBuilderHelper.NULL);
    }

    @Override
    public Collection<Processable> getChildren() {
        return Collections.singletonList(value);
    }
}
