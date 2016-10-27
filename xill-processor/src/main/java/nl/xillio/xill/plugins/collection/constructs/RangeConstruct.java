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

import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.MetaExpressionIterator;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.plugins.collection.data.RangeIterator;

/**
 * This construct will return an iterator of the given range and step
 *
 * @author Pieter Soels
 */
public class RangeConstruct extends Construct {
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
        RangeIterator iterator;
        if(step.isNull()){
            iterator = new RangeIterator(start.getNumberValue(), end.getNumberValue());
        } else {
            iterator = new RangeIterator(start.getNumberValue(), end.getNumberValue(), step.getNumberValue());
        }

        MetaExpression result = fromValue("[Ranged iterator]");
        result.storeMeta(new MetaExpressionIterator<>(iterator, ExpressionBuilderHelper::fromValue));
        return result;
    }

    private MetaExpression validateInput(MetaExpression start, MetaExpression end, MetaExpression step) {
        if (start.getNumberValue().intValue() < end.getNumberValue().intValue()) {
            if (step.isNull()) {
                step = fromValue(1);
            } else if (step.getNumberValue().intValue() > 0) {
                throw new InvalidUserInputException(
                        "The given step should be positive (or null) when the start-value is smaller than the end-value",
                        start.getStringValue(),
                        "A step-value greater than 0 when start-value is lesser than end-value",
                        "var step = 1;"
                );
            }
        } else if (end.getNumberValue().intValue() < start.getNumberValue().intValue()) {
            if (step.isNull()) {
                return fromValue(-1.0);
            } else if (step.getNumberValue().intValue() < 0) {
                throw new InvalidUserInputException(
                        "The given step should be negative (or null) when the end-value is smaller than the start-value",
                        start.getStringValue(),
                        "A step-value lesser than 0 when start-value is lesser than end-value",
                        "var step = -1;"
                );
            }
        } else {
            throw new InvalidUserInputException(
                    "The given start-value and end-value must not be the same",
                    "start: " + start.getStringValue() + " , end: " + end.getStringValue(),
                    "A start-value and an end-value that are not the same",
                    "var start = 0; var end = 10;"
            );
        }
        return step;
    }
}