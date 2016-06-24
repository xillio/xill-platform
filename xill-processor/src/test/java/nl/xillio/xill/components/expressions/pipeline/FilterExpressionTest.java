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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.MetaExpressionIterator;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.components.expressions.pipeline.FilterExpression;
import nl.xillio.xill.components.expressions.pipeline.MapExpression;
import nl.xillio.xill.components.instructions.FunctionDeclaration;
import nl.xillio.xill.components.instructions.InstructionSet;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;


public class FilterExpressionTest extends TestUtils {
    @Test
    public void testNormal() {
        MetaExpression input = parseObject(Arrays.asList(1, 2, 3, 4, 5));
        FilterExpression expression = new FilterExpression(input);
        expression.setFunction(new IsEvenFunction());

        MetaExpression result = expression.process(mock(Debugger.class)).get();
        MetaExpressionIterator iterator = result.getMeta(MetaExpressionIterator.class);

        assertEquals(iterator.next(), fromValue(2));
        assertEquals(iterator.next(), fromValue(4));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testNull() {
        MapExpression expression = new MapExpression(NULL);
        expression.setFunction(new IsEvenFunction());

        MetaExpression result = expression.process(mock(Debugger.class)).get();
        MetaExpressionIterator iterator = result.getMeta(MetaExpressionIterator.class);

        assertFalse(iterator.hasNext());
    }

    @Test(expectedExceptions = NoSuchElementException.class)
    public void testException() {
        MapExpression expression = new MapExpression(NULL);
        expression.setFunction(new IsEvenFunction());

        MetaExpression result = expression.process(mock(Debugger.class)).get();
        MetaExpressionIterator iterator = result.getMeta(MetaExpressionIterator.class);

        iterator.next();
    }

    private class IsEvenFunction extends FunctionDeclaration {

        public IsEvenFunction() {
            super(mock(InstructionSet.class), new ArrayList<>());
        }

        @Override
        public InstructionFlow<MetaExpression> run(Debugger debugger, List<MetaExpression> arguments) throws RobotRuntimeException {
            boolean result = ((arguments.get(0).getNumberValue().intValue() & 1) == 0);
            return InstructionFlow.doResume(fromValue(result));
        }
    }

}