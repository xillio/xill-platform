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
package nl.xillio.xill.plugins.math.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.math.services.math.MathOperations;

import java.util.List;

/**
 * <p>
 * The construct of the Random function.
 * </p>
 * <p>
 * The random function receives a {@link ExpressionDataType#LIST} or a {@link ExpressionDataType#ATOMIC} value.
 * </p>
 * <p>
 * In the first case it returns a random value from the {@link ExpressionDataType#LIST}.
 * </p>
 * <p>
 * In the latter it generates a random number
 * </p>
 *
 * @author Ivor
 */
public class RandomConstruct extends Construct {

    @Inject
    private MathOperations mathService;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(value -> process(value, mathService), new Argument("value", NULL, LIST, ATOMIC));
    }

    static MetaExpression process(final MetaExpression value, final MathOperations math) {
        // In case of a list, convert it to a list of MetaExpressions and return a random index
        if (value.getType() == LIST) {
            @SuppressWarnings("unchecked")
            List<MetaExpression> list = (List<MetaExpression>) value.getValue();
            return list.get((int) math.random(list.size()));
        }
        // In case of a number below zero or a null value given, return a random double
        else if (value.getNumberValue().longValue() <= 0) {
            return fromValue(math.random());
        }
        // else we got a positive number, return a number between 0 and the given number.
        else {
            return fromValue(math.random(value.getNumberValue().longValue()));
        }
    }
}
