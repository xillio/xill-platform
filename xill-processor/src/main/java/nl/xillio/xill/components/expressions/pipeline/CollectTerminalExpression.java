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

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;

/**
 * This class represents the implementation of the collect function. This function will build a list from all elements
 * in an iterable argument.
 *
 * @author Thomas Biesaart
 * @author Andrea Parrilli
 */
public class CollectTerminalExpression extends AbstractPipelineTerminalExpression {

    public CollectTerminalExpression(Processable input) {
        super(input);
    }

    @Override
    protected MetaExpression reduce(MetaExpression inputValue, Debugger debugger) {
        // Create the iterator
        try (WrappingIterator iterator = iterate(inputValue)) {

            List<MetaExpression> output = new ArrayList<>();
            // Walk the iterator
            while (iterator.hasNext()) {
                output.add(iterator.next());
            }

            return fromValue(output);
        } catch(ConcurrentModificationException e){
            throw new RobotConcurrentModificationException(e);
        }
    }
}
