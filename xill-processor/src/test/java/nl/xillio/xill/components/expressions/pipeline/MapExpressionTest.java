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
import nl.xillio.xill.components.expressions.pipeline.MapExpression;
import nl.xillio.xill.components.instructions.FunctionDeclaration;
import nl.xillio.xill.components.instructions.InstructionSet;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;


public class MapExpressionTest extends TestUtils {
    @Test
    public void testNormal() {
        MetaExpression input = parseObject(Arrays.asList(1, 2, 3, 4, 5));
        MapExpression expression = new MapExpression(input);
        expression.setFunction(new IdentityFunction());

        MetaExpression result = expression.process(mock(Debugger.class)).get();
        MetaExpressionIterator iterator = result.getMeta(MetaExpressionIterator.class);

        assertEquals(iterator.next(), fromValue(1));
        assertEquals(iterator.next(), fromValue(2));
        assertEquals(iterator.next(), fromValue(3));
        assertEquals(iterator.next(), fromValue(4));
        assertEquals(iterator.next(), fromValue(5));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testNull() {
        MapExpression expression = new MapExpression(NULL);
        expression.setFunction(new IdentityFunction());

        MetaExpression result = expression.process(mock(Debugger.class)).get();
        MetaExpressionIterator iterator = result.getMeta(MetaExpressionIterator.class);

        assertFalse(iterator.hasNext());
    }

    private class IdentityFunction extends FunctionDeclaration {

        public IdentityFunction() {
            super(mock(InstructionSet.class), new ArrayList<>());
        }

        @Override
        public InstructionFlow<MetaExpression> run(Debugger debugger, List<MetaExpression> arguments) throws RobotRuntimeException {
            return InstructionFlow.doResume(arguments.get(0));
        }
    }

}