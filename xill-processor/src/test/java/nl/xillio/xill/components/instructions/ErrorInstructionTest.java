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
package nl.xillio.xill.components.instructions;

import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Processable;
import nl.xillio.xill.debugging.ErrorBlockDebugger;
import nl.xillio.xill.debugging.XillDebugger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;

public class ErrorInstructionTest {

    private XillDebugger xillDebugger;
    private InstructionSet doBlock;
    private InstructionSet errorBlock;
    private InstructionSet successBlock;
    private InstructionSet finallyBlock;
    private ErrorBlockDebugger errorDebugger;
    private InstructionFlow<MetaExpression> result;

    @BeforeMethod
    public void setUp() {
        xillDebugger = new XillDebugger();
        doBlock = new InstructionSet(xillDebugger);
        errorBlock = new InstructionSet(xillDebugger);
        successBlock = new InstructionSet(xillDebugger);
        finallyBlock = new InstructionSet(xillDebugger);
        errorDebugger = mock(ErrorBlockDebugger.class);


    }

    @Test
    public void testProcessSuccess() {
        result = (InstructionFlow<MetaExpression>) mock(InstructionFlow.class);
        InstructionSet mockDoBlock = mock(InstructionSet.class);
        MetaExpression mockMeta = mock(MetaExpression.class);
        ErrorInstruction instruction = new ErrorInstruction(mockDoBlock, successBlock, errorBlock, finallyBlock, null);

        when(result.hasValue()).thenReturn(false);
        when(result.get()).thenReturn(mockMeta);
        when(errorDebugger.hasError()).thenReturn(false);
        when(mockDoBlock.process(errorDebugger)).thenReturn(result);

        InstructionFlow<MetaExpression> returnedValue = instruction.process(xillDebugger, errorDebugger);

        verify(result, times(2)).hasValue();

    }

    @Test
    public void testProcessReturn() {
        result = (InstructionFlow<MetaExpression>) mock(InstructionFlow.class);
        MetaExpression mockMeta = mock(MetaExpression.class);
        InstructionSet mockDoBlock = mock(InstructionSet.class);
        ErrorInstruction instruction = new ErrorInstruction(mockDoBlock, successBlock, errorBlock, finallyBlock, null);

        when(mockDoBlock.process(errorDebugger)).thenReturn(result);
        when(result.hasValue()).thenReturn(true);
        when(result.get()).thenReturn(mockMeta);

        InstructionFlow<MetaExpression> returnedValue = instruction.process(xillDebugger, errorDebugger);

        verify(result, times(2)).get();

        assertNotSame(returnedValue, InstructionFlow.doResume());
    }

    @Test
    public void testProcessFailNoCause() {
        result = (InstructionFlow<MetaExpression>) mock(InstructionFlow.class);
        InstructionFlow<MetaExpression> resultError = (InstructionFlow<MetaExpression>) mock(InstructionFlow.class);
        MetaExpression metaExpressionResult = mock(MetaExpression.class);

        InstructionSet mockDoBlock = mock(InstructionSet.class);
        InstructionSet mockErrorBlock = mock(InstructionSet.class);
        ErrorInstruction instruction = new ErrorInstruction(mockDoBlock, successBlock, mockErrorBlock, finallyBlock, null);

        when(mockDoBlock.process(errorDebugger)).thenReturn(result);
        when(mockErrorBlock.process(xillDebugger)).thenReturn(resultError);

        when(result.hasValue()).thenReturn(false);
        when(errorDebugger.hasError()).thenReturn(true);

        instruction.process(xillDebugger, errorDebugger);

        // This test is to make sure there are no NPEs even though there is no cause
    }

    @Test
    public void testProcessFailWithCause() {
        result = (InstructionFlow<MetaExpression>) mock(InstructionFlow.class);
        InstructionFlow<MetaExpression> resultError = (InstructionFlow<MetaExpression>) mock(InstructionFlow.class);
        Throwable throwableMock = mock(Throwable.class);
        InstructionSet mockDoBlock = mock(InstructionSet.class);
        InstructionSet mockErrorBlock = mock(InstructionSet.class);
        Processable mockProcessable = mock(Processable.class);
        VariableDeclaration cause = new VariableDeclaration(mockProcessable, "fail");

        ErrorInstruction instruction = new ErrorInstruction(mockDoBlock, successBlock, errorBlock, finallyBlock, cause);
        when(mockDoBlock.process(errorDebugger)).thenReturn(result);
        when(mockErrorBlock.process(xillDebugger)).thenReturn(resultError);
        when(errorDebugger.getError()).thenReturn(throwableMock);
        when(result.hasValue()).thenReturn(false);
        when(errorDebugger.hasError()).thenReturn(true);

        instruction.process(xillDebugger, errorDebugger);

        verify(throwableMock, times(1)).getMessage();
    }

    @Test
    public void testProcessOptionals() {
        result = (InstructionFlow<MetaExpression>) mock(InstructionFlow.class);
        InstructionSet mockSuccesBlock = mock(InstructionSet.class);
        InstructionSet mockErrorBlock = mock(InstructionSet.class);
        InstructionSet mockFinallyBlock = mock(InstructionSet.class);

        ErrorInstruction instruction = new ErrorInstruction(doBlock, null, mockErrorBlock, null, null);

        when(result.returns()).thenReturn(false);

        InstructionFlow<MetaExpression> returnedValue = instruction.process(xillDebugger);

        verify(mockSuccesBlock, never()).process(any());
        verify(mockFinallyBlock, never()).process(any());

    }

    @Test
    public void testGetChildren() {

        Processable mockProcessable = mock(Processable.class);
        VariableDeclaration cause = new VariableDeclaration(mockProcessable, "fail");

        List<Processable> children = new ArrayList<>();
        children.add(doBlock);
        children.add(successBlock);
        children.add(errorBlock);
        children.add(finallyBlock);
        children.add(cause);

        ErrorInstruction instruction = new ErrorInstruction(doBlock, successBlock, errorBlock, finallyBlock, cause);

        Collection<Processable> returnValue = instruction.getChildren();

        assertEquals(new HashSet<>(returnValue), new HashSet<>(children));
    }
}
