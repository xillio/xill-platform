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
package nl.xillio.xill.plugins.collection.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.MetaExpressionIterator;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.plugins.collection.data.range.RangeIteratorFactory;

import java.util.Iterator;

/**
 * This construct will return an iterator of the given range and step
 *
 * @author Pieter Soels
 */
public class RangeConstruct extends Construct {
    private final RangeIteratorFactory rangeIteratorFactory;

    @Inject
    public RangeConstruct(RangeIteratorFactory rangeIteratorFactory) {
        this.rangeIteratorFactory = rangeIteratorFactory;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("start", ATOMIC),
                new Argument("end", ATOMIC),
                new Argument("step", NULL, ATOMIC)
        );
    }

    private MetaExpression process(MetaExpression start, MetaExpression end, MetaExpression step) {
        Iterator<Number> iterator;
        try {
            iterator = rangeIteratorFactory.createIterator(
                    start.getNumberValue(),
                    end.getNumberValue(),
                    step.isNull() ? null : step.getNumberValue());
        } catch (IllegalArgumentException e) {
            throw new InvalidUserInputException(
                    e.getMessage(),
                    "Start: " + start.getStringValue() +
                            ", end: " + end.getStringValue() +
                            ", step: " + step.getStringValue(),
                    "Correct numbers values for start, end and step.",
                    "Collection.range(0, 10, 1);",
                    e);
        }

        MetaExpression result = fromValue("[Ranged iterator]");
        result.storeMeta(new MetaExpressionIterator<>(iterator, ExpressionBuilderHelper::fromValue));
        return result;
    }
}