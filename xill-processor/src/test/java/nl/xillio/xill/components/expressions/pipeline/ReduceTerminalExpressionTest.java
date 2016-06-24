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
import nl.xillio.xill.components.instructions.FunctionDeclaration;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.primitives.Ints.asList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;


public class ReduceTerminalExpressionTest extends TestUtils {
    @Test
    public void testCopyList() {
        List<Integer> input = asList(1, 2, 51, 5, 31, 16510);

        MetaExpression result = run(emptyList(), input, mockDeclaration());

        assertEquals(result, parseObject(input));
    }

    private MetaExpression run(MetaExpression accumulator, Object input, FunctionDeclaration function) {
        return run(accumulator, parseObject(input), function);
    }

    private MetaExpression run(MetaExpression accumulator, MetaExpression inputValue, FunctionDeclaration function) {
        ReduceTerminalExpression expression = new ReduceTerminalExpression(accumulator, inputValue);
        expression.setFunction(function);
        return expression.process(mock(Debugger.class)).get();
    }

    /**
     * This will create a mock function that will create a copy of the inputs.
     *
     * @return the function
     */
    private FunctionDeclaration mockDeclaration() {
        FunctionDeclaration result = mock(FunctionDeclaration.class);

        when(result.run(any(), anyList())).thenAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] inputs = invocation.getArguments();
                List arguments = (List) inputs[1];
                MetaExpression accumulator = (MetaExpression) arguments.get(0);
                MetaExpression element = (MetaExpression) arguments.get(1);

                element.registerReference();
                accumulator.<List<MetaExpression>>getValue().add(element);

                return InstructionFlow.doResume(accumulator);
            }
        });

        return result;
    }
}