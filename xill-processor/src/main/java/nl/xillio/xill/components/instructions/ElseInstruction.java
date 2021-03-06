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
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Processable;

import java.util.Arrays;
import java.util.Collection;

/**
 * This {@link Instruction} represents a stop in an instruction set.
 */
public class ElseInstruction extends CompoundInstruction {

    private final InstructionSet elseInstructions;

    /**
     * Create a new {@link ElseInstruction}
     *
     * @param elseInstructions A collection of elseInstructions.
     */
    public ElseInstruction(final InstructionSet elseInstructions) {
        this.elseInstructions = elseInstructions;
        elseInstructions.setParentInstruction(this);

    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) {
        return elseInstructions.process(debugger);
    }

    @Override
    public Collection<Processable> getChildren() {
        return Arrays.asList(elseInstructions);
    }

    @Override
    public boolean preventDebugging() {
        return false;
    }

}
