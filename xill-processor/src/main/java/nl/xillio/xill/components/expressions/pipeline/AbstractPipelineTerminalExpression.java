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
import nl.xillio.xill.api.errors.RobotConcurrentModificationException;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;

/**
 * This class represent the base implementation of a function that will reduce an iterable input to a summary.
 *
 * @author Thomas Biesaart
 * @author Andrea Parrilli
 */
public abstract class AbstractPipelineTerminalExpression implements Processable {
    private final Processable input;

    public AbstractPipelineTerminalExpression(Processable input) {
        this.input = input;
    }

    @Override
    public InstructionFlow<MetaExpression> process(Debugger debugger) {
        MetaExpression iterableValue = input.process(debugger).get();
        MetaExpression reduced;
        try {
            reduced = reduce(iterableValue, debugger);
        }catch(ConcurrentModificationException e){
            throw new RobotConcurrentModificationException(e);
        }
        return InstructionFlow.doResume(reduced);
    }

    protected WrappingIterator iterate(MetaExpression expression) {
        return WrappingIterator.identity(expression);
    }

    protected abstract MetaExpression reduce(MetaExpression inputValue, Debugger debugger);

    @Override
    public Collection<Processable> getChildren() {
        return Collections.singletonList(input);
    }
}
