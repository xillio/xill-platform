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
package nl.xillio.xill.components.expressions.pipeline;

import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.*;
import nl.xillio.xill.api.errors.RobotConcurrentModificationException;
import nl.xillio.xill.components.expressions.FunctionParameterExpression;
import nl.xillio.xill.components.instructions.FunctionDeclaration;

import java.util.Collections;
import java.util.ConcurrentModificationException;

/**
 * This class represents the implementation of the foreach function. This function will apply a function to every element
 * in an iterable argument and return nothing.
 *
 * @author Thomas Biesaart
 * @author Andrea Parrilli
 */
public class ForeachTerminalExpression extends AbstractPipelineTerminalExpression implements FunctionParameterExpression {
    private FunctionDeclaration function;

    public ForeachTerminalExpression(Processable input) {
        super(input);
    }

    @Override
    protected MetaExpression reduce(MetaExpression inputValue, Debugger debugger) {
        // Create the iterator
        try (WrappingIterator iterator = iterate(inputValue)) {

            // Walk the iterator
            while (iterator.hasNext()) {
                InstructionFlow<MetaExpression> result = function.run(debugger, Collections.singletonList(iterator.next()));
                if (result.hasValue() && !result.get().isDisposalPrevented()) {
                    result.get().registerReference();
                    result.get().releaseReference();
                }
            }

            // Return the result
            return ExpressionBuilder.NULL;
        } catch(ConcurrentModificationException e){
            throw new RobotConcurrentModificationException(e);
        }
    }

    @Override
    public void setFunction(FunctionDeclaration function) {
        this.function = function;
    }
}
