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
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.util.Arrays;
import java.util.Collection;

/**
 * This {@link Instruction} represents the while looping mechanism.
 */
public class WhileInstruction extends CompoundInstruction {

    private final ExpressionInstruction condition;
    private final InstructionSet instructionSet;

    /**
     * Instantiate a {@link WhileInstruction} from a condition and an InstructionSet
     *
     * @param condition      the condition
     * @param instructionSet the set that should be processes until the condition hits false
     */
    public WhileInstruction(final Processable condition, final InstructionSet instructionSet) {
        this.condition = new ExpressionInstruction(condition);
        this.instructionSet = instructionSet;
        instructionSet.setParentInstruction(this);
    }

    @Override
    public void setHostInstruction(InstructionSet hostInstruction) {
        super.setHostInstruction(hostInstruction);
        this.condition.setHostInstruction(hostInstruction);
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) throws RobotRuntimeException {
        while (check(debugger) && !debugger.shouldStop()) {
            InstructionFlow<MetaExpression> result = instructionSet.process(debugger);

            if (result.returns()) {
                return result;
            }

            if (result.breaks()) {
                break;
            }
        }

        return InstructionFlow.doResume();
    }

    @Override
    public void setPosition(CodePosition position) {
        super.setPosition(position);
        condition.setPosition(position);
    }

    private boolean check(Debugger debugger) {
        debugger.startInstruction(condition);
        if (debugger.shouldStop()) {
            debugger.endInstruction(condition, InstructionFlow.doReturn(ExpressionBuilderHelper.NULL));
            return false;
        }
        InstructionFlow<MetaExpression> result = condition.process(debugger);
        MetaExpression expression = result.get();
        expression.registerReference();
        boolean isValue = expression.getBooleanValue();
        debugger.endInstruction(condition, result);
        expression.releaseReference();
        condition.clear();
        return isValue;
    }

    @Override
    public Collection<Processable> getChildren() {
        return Arrays.asList(condition, instructionSet);
    }
}
