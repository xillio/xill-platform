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
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


public class ForeachTerminalExpressionTest extends TestUtils {

    @Test
    public void testList() {
        FunctionDeclaration declaration = mockDeclaration();
        run(asList(1, 2, 3), declaration);
        verify(declaration, times(3)).run(any(), anyList());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCloseException() {
        MetaExpression input = fromValue("I will be closed");
        run(input, mockDeclaration());
        input.getType(); // This will trigger an exception if the expression is closed
    }

    private void run(Object input, FunctionDeclaration function) {
        run(parseObject(input), function);
    }

    private void run(MetaExpression inputValue, FunctionDeclaration function) {
        ForeachTerminalExpression expression = new ForeachTerminalExpression(inputValue);
        expression.setFunction(function);
        expression.process(mock(Debugger.class));
    }

    private FunctionDeclaration mockDeclaration() {
        FunctionDeclaration declaration = mock(FunctionDeclaration.class);
        when(declaration.run(any(), anyList())).thenReturn(InstructionFlow.doResume(NULL));
        return declaration;
    }
}