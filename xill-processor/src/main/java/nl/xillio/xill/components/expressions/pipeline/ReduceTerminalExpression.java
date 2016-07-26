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
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Processable;
import nl.xillio.xill.api.components.WrappingIterator;
import nl.xillio.xill.api.errors.RobotConcurrentModificationException;
import nl.xillio.xill.components.expressions.FunctionParameterExpression;
import nl.xillio.xill.components.instructions.FunctionDeclaration;

import java.util.Arrays;
import java.util.ConcurrentModificationException;

/**
 * This class represents the implementation of the reduce function. This function takes an iterable and an accumulator
 * so it can produce a summary of the iterator.
 *
 * @author Thomas Biesaart
 * @author Andrea Parrilli
 */
public class ReduceTerminalExpression extends AbstractPipelineTerminalExpression implements FunctionParameterExpression {
    private final Processable accumulator;
    private FunctionDeclaration function;

    public ReduceTerminalExpression(Processable accumulator, Processable input) {
        super(input);
        this.accumulator = accumulator;
    }

    @Override
    protected MetaExpression reduce(MetaExpression inputValue, Debugger debugger) {
        // Create the iterator
        try (WrappingIterator iterator = iterate(inputValue)) {
            // Get the initial accumulator value
            MetaExpression workingValue = accumulator.process(debugger).get();
            workingValue.registerReference();

            // Walk the iterator
            while (iterator.hasNext()) {
                MetaExpression next = iterator.next();
                workingValue = function.run(debugger, Arrays.asList(workingValue, next)).get();
            }

            // Return the result
            return workingValue;
        } catch(ConcurrentModificationException e){
            throw new RobotConcurrentModificationException(e);
        }
    }

    @Override
    public void setFunction(FunctionDeclaration function) {
        this.function = function;
    }
}
