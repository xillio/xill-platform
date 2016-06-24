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
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Processable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This {@link Instruction} represents a stop in an instruction set.
 */
public class IfInstruction extends CompoundInstruction {

    private final Processable condition;
    private final InstructionSet instructionSet;

    /**
     * Create a new {@link IfInstruction}
     *
     * @param condition      The condition passed to the if-instruction.
     * @param instructionSet Relevant for processing the if-instruction.
     */
    public IfInstruction(final Processable condition, final InstructionSet instructionSet) {
        this.condition = condition;
        this.instructionSet = instructionSet;
        instructionSet.setParentInstruction(this);
    }

    /**
     * Check if the condition for this block is true.
     *
     * @param debugger the debugger to use for processing
     * @return true if and only if the condition of this statement evaluates to true
     */
    public boolean isTrue(final Debugger debugger) {
        try (ExpressionInstruction conditionInstruction = new ExpressionInstruction(condition)) {
            conditionInstruction.setHostInstruction(getHostInstruction());
            conditionInstruction.setPosition(getPosition());
            debugger.startInstruction(conditionInstruction);
            if (debugger.shouldStop()) {
                debugger.endInstruction(conditionInstruction, InstructionFlow.doReturn(ExpressionBuilderHelper.NULL));
                return false;
            }
            InstructionFlow<MetaExpression> result = conditionInstruction.process(debugger);
            result.get().registerReference();
            debugger.endInstruction(conditionInstruction, result);
            return result.get().getBooleanValue();
        }
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) {
        return instructionSet.process(debugger);
    }

    @Override
    public void setPosition(CodePosition position) {
        super.setPosition(position);
    }

    @Override
    public Collection<Processable> getChildren() {
        return new ArrayList<>();
    }

}
