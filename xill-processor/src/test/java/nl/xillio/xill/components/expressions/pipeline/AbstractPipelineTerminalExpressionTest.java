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
import nl.xillio.xill.api.components.Processable;
import nl.xillio.xill.api.errors.RobotConcurrentModificationException;
import org.testng.annotations.Test;

import java.util.ConcurrentModificationException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractPipelineTerminalExpressionTest extends TestUtils{

    @Test(expectedExceptions = RobotConcurrentModificationException.class)
    public void testConcurrentException(){
        Processable input = mock(PipelineExpression.class);
        when(input.process(any())).thenReturn(InstructionFlow.doReturn(fromValue(true)));
        AbstractPipelineTerminalExpression expression = new AbstractPipelineTerminalExpression(input){
            protected MetaExpression reduce(MetaExpression inputValue, Debugger debugger){
                throw new ConcurrentModificationException();
            }
        };
        expression.process(mock(Debugger.class));
    }

}