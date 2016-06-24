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
package nl.xillio.xill.debugging;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.components.instructions.FunctionDeclaration;
import nl.xillio.xill.components.instructions.Instruction;
import nl.xillio.xill.components.instructions.VariableDeclaration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import xill.lang.xill.Target;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

/**
 * Tests for {@link XillDebugger}
 *
 * @author Geert Konijnendijk
 */
public class XillDebuggerTest extends TestUtils{

    XillDebugger debugger;

    /**
     * Create the debugger
     */
    @BeforeMethod
    public void setupDebugger(){
        debugger = new XillDebugger();
    }

    /**
     * Test {@link XillDebugger#getVariableValue(Object, int)} when getting a variable value from the robot root.
     */
    @Test
    public void testGetVariableValueRoot() {
        // Mock
        VariableDeclaration var = mock(VariableDeclaration.class, RETURNS_DEEP_STUBS);
        Target target = mock(Target.class);
        MetaExpression result = mockVariable(target, var, 0)[0];
        startInstructions(1);

        // Run
        MetaExpression value = debugger.getVariableValue(target, 1);

        // Verify
        verify(var).peek(0);

        // Assert
        assertSame(value, result);
    }

    @Test
    public void testGetVariableValueRootFromFunction() {
        // Mock
        VariableDeclaration var = mock(VariableDeclaration.class, RETURNS_DEEP_STUBS);
        when(var.getHostInstruction().getParentInstruction()).thenReturn(mock(FunctionDeclaration.class));
        Target target = mock(Target.class);
        MetaExpression result = mockVariable(target, var, 0)[0];
        startInstructions(2);

        // Run
        MetaExpression value = debugger.getVariableValue(target, 2);

        // Verify
        verify(var).peek(0);

        // Assert
        assertSame(value, result);
    }

    /**
     * Test {@link XillDebugger#getVariableValue(Object, int)} when a function is called.
     */
    @Test
    public void testGetVariableValueFunction() {
        // Mock
        VariableDeclaration var = mock(VariableDeclaration.class, RETURNS_DEEP_STUBS);
        when(var.getHostInstruction().getParentInstruction()).thenReturn(mock(FunctionDeclaration.class));
        Target target = mock(Target.class);
        MetaExpression result = mockVariable(target, var, 1)[0];
        startInstructions(2);

        // Run
        MetaExpression value = debugger.getVariableValue(target, 1);

        // Verify
        verify(var, never()).peek(0);

        // Assert
        assertSame(value, result);
    }

    /**
     * Test {@link XillDebugger#getVariableValue(Object, int)} when a recursive function has been called three times and
     * the value for a variable on the second call is requested.
     */
    @Test
    public void testGetVariableValueRecursion() {
        // Mock
        VariableDeclaration var = mock(VariableDeclaration.class, RETURNS_DEEP_STUBS);
        when(var.getHostInstruction().getParentInstruction()).thenReturn(mock(FunctionDeclaration.class));
        Target target = mock(Target.class);
        MetaExpression[] results = mockVariable(target, var, 1,2,3);
        startInstructions(4);

        // Run
        MetaExpression value = debugger.getVariableValue(target, 2);

        // Verify
        verify(var, never()).peek(1);
        verify(var, never()).peek(0);

        // Assert
        assertSame(value, results[1]);
    }

    /**
     * Start a number of instructions on the tested debugger
     * @param num The number of instructions to start
     */
    private void startInstructions(int num) {
        for (int i=0; i<num; i++) {
            debugger.startInstruction(mock(Instruction.class));
        }
    }

    /**
     * Mock a variables map containing a single variable and add it to the debugger
     * @param positions The stack positions to put values at
     * @return An array of resulting assigned values corresponding to the input positions
     */
    private MetaExpression[] mockVariable(Target target, VariableDeclaration var, int... positions) {
        DebugInfo info = new DebugInfo();
        Map<Target, VariableDeclaration> variables = new HashMap<>();


        MetaExpression[] results = new MetaExpression[positions.length];
        for (int i=0; i<positions.length; i++) {
            MetaExpression result = mockExpression(ATOMIC);
            results[i] = result;
            when(var.peek(positions[i])).thenReturn(result);
        }
        variables.put(target, var);
        info.setVariables(variables);
        debugger.addDebugInfo(info);
        return results;
    }

}
