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

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;
import static nl.xillio.xill.api.components.ExpressionDataType.ATOMIC;

/**
 * This class represents the implementation of the consume function. This function will iterate an iterable argument and
 * return the number processed items.
 *
 * @author Thomas Biesaart
 * @author Andrea Parrilli
 */
public class ConsumeTerminalExpression extends AbstractPipelineTerminalExpression {

    public ConsumeTerminalExpression(Processable input) {
        super(input);
    }

    @Override
    protected MetaExpression reduce(MetaExpression inputValue, Debugger debugger) {
        long count = process(inputValue);

        return fromValue(count);
    }

    private long process(MetaExpression inputValue) {
        if (inputValue.getType() == ATOMIC) {
            return count(inputValue);
        }
        return inputValue.getSize().longValue();
    }

    private long count(MetaExpression inputValue) {
        long result = 0;
        try (WrappingIterator iterator = iterate(inputValue)) {
            while (iterator.hasNext()) {
                MetaExpression expression = iterator.next();
                if (!expression.isDisposalPrevented()) {
                    expression.registerReference();
                    expression.releaseReference();
                }
                result++;
            }
        }
        return result;
    }
}
