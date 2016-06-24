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
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This {@link Instruction} represents the condition based branching mechanism.
 */
public class IfInstructionBlock extends CompoundInstruction {
    private final List<IfInstruction> conditionInstructions;
    private final ElseInstruction elseInstruction;

    /**
     * Create a new {@link IfInstructionBlock}.
     *
     * @param conditionals    the if instructions
     * @param elseInstruction the else instruction
     */
    public IfInstructionBlock(final List<IfInstruction> conditionals, final ElseInstruction elseInstruction) {
        conditionInstructions = conditionals;
        this.elseInstruction = elseInstruction;
    }

    @Override
    public void setHostInstruction(InstructionSet hostInstruction) {
        super.setHostInstruction(hostInstruction);
        conditionInstructions.forEach(inst -> inst.setHostInstruction(hostInstruction));
        if (elseInstruction != null) {
            elseInstruction.setHostInstruction(hostInstruction);
        }
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) throws RobotRuntimeException {

        // Find the first if instruction
        for (IfInstruction instruction : conditionInstructions) {

            if (instruction.isTrue(debugger)) {
                return instruction.process(debugger);
            }
        }

        // Process the else instruction
        if (elseInstruction != null) {
            return elseInstruction.process(debugger);
        }

        // There was no else instruction
        return InstructionFlow.doResume();
    }

    @Override
    public Collection<Processable> getChildren() {
        List<Processable> children = new ArrayList<>();
        children.addAll(conditionInstructions);

        return children;
    }
}
