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
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Processable;
import nl.xillio.xill.api.components.WrappingIterator;
import nl.xillio.xill.components.expressions.FunctionParameterExpression;
import nl.xillio.xill.components.instructions.FunctionDeclaration;

import java.util.Arrays;
import java.util.Collection;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;

/**
 * This class represents the base for an iterable to iterable pipeline function.
 *
 * @author Thomas Biesaart
 */
abstract class PipelineExpression implements Processable, FunctionParameterExpression {
    private final Processable input;
    private FunctionDeclaration functionDeclaration;

    public PipelineExpression(Processable input) {
        this.input = input;
    }

    @Override
    public void setFunction(FunctionDeclaration function) {
        this.functionDeclaration = function;
    }

    @Override
    public InstructionFlow<MetaExpression> process(Debugger debugger) {
        // Evaluate the input argument
        MetaExpression inputValue = input.process(debugger).get();

        // Wrap it
        MetaExpression result = fromValue(String.format("%s(%s)", describe(), inputValue.getStringValue()));
        result.storeMeta(wrap(inputValue, functionDeclaration, debugger));
        return InstructionFlow.doResume(result);
    }

    protected abstract WrappingIterator wrap(MetaExpression input, FunctionDeclaration functionDeclaration, Debugger debugger);

    protected abstract String describe();


    @Override
    public Collection<Processable> getChildren() {
        return Arrays.asList(input, functionDeclaration);
    }
}
