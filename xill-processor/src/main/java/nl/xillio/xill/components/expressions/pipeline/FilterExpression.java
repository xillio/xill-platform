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
import nl.xillio.xill.components.instructions.FunctionDeclaration;

import java.util.Collections;
import java.util.NoSuchElementException;

/**
 * This class represents the factory for the filter construction. This construction allows you to remove certain elements
 * from an iterable.
 *
 * @author Thomas Biesaart
 */
public class FilterExpression extends PipelineExpression {

    public FilterExpression(Processable input) {
        super(input);
    }

    @Override
    protected WrappingIterator wrap(MetaExpression input, FunctionDeclaration functionDeclaration, Debugger debugger) {
        return new FilterIterator(input, functionDeclaration, debugger);
    }

    @Override
    protected String describe() {
        return "filter";
    }

    /**
     * This class represents the implementation of the filter runtime.
     * It will cache 1 item continuously to check if it matches the predicate.
     */
    private class FilterIterator extends WrappingIterator {
        private final FunctionDeclaration function;
        private final Debugger debugger;
        private MetaExpression next;

        public FilterIterator(MetaExpression host, FunctionDeclaration function, Debugger debugger) {
            super(host);
            this.function = function;
            this.debugger = debugger;
        }

        @Override
        public boolean hasNext() {
            cacheNext();
            return next != null;
        }

        private void cacheNext() {
            while (next == null && super.hasNext()) {
                MetaExpression value = super.next();
                value.registerReference();
                MetaExpression shouldKeep = function.run(debugger, Collections.singletonList(value)).get();
                if (shouldKeep.getBooleanValue()) {
                    // We are done with this but since we are returning it we don't want to dispose
                    boolean isPrevented = value.isDisposalPrevented();

                    value.preventDisposal();
                    value.releaseReference();

                    if(!isPrevented) {
                        value.allowDisposal();
                    }

                    next = value;
                } else {
                    // We are done with this
                    value.releaseReference();
                }
            }
        }

        @Override
        public MetaExpression next() {
            if (!hasNext()) {
                throw new NoSuchElementException("This iterator is empty");
            }
            MetaExpression result = next;
            next = null;
            return result;
        }

        @Override
        protected MetaExpression transformItem(MetaExpression item) {
            return item;
        }
    }
}
