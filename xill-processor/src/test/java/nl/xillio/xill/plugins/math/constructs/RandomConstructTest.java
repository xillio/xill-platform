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

import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.math.services.math.MathOperations;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;

/**
 * Test the {@link RandomConstruct}.
 */
public class RandomConstructTest extends ExpressionBuilderHelper {

    /**
     * Test the process method with a positive value given.
     */
    @Test
    public void processPositiveLong() {
        // Mock
        long numberValue = 42;
        MetaExpression value = mock(MetaExpression.class);
        when(value.getNumberValue()).thenReturn(numberValue);

        long mathReturnValue = 12;
        MathOperations math = mock(MathOperations.class);
        when(math.random(numberValue)).thenReturn(mathReturnValue);

        // Run
        MetaExpression result = RandomConstruct.process(value, math);

        // Verify
        verify(math, times(1)).random(numberValue);

        // Assert
        Assert.assertEquals(result.getNumberValue().longValue(), mathReturnValue);

    }

    /**
     * Test the process method with a negative value given.
     */
    @Test
    public void processNegativeLong() {
        // Mock
        long numberValue = -42;
        MetaExpression value = mock(MetaExpression.class);
        when(value.getNumberValue()).thenReturn(numberValue);

        double mathReturnValue = 0.01;
        MathOperations math = mock(MathOperations.class);
        when(math.random()).thenReturn(mathReturnValue);

        // Run
        MetaExpression result = RandomConstruct.process(value, math);

        // Verify
        verify(math, times(1)).random();

        // Assert
        Assert.assertEquals(result.getNumberValue().doubleValue(), mathReturnValue);
    }

    /**
     * Test the process method with a LIST value given.
     */
    @Test
    public void processList() {
        // Mock
        MetaExpression value = mock(MetaExpression.class);
        when(value.getValue()).thenReturn(Arrays.asList(
                fromValue("Test"),
                fromValue("Command"),
                fromValue("T-1")));
        when(value.getType()).thenReturn(LIST);

        long mathReturnValue = 1;
        MathOperations math = mock(MathOperations.class);
        when(math.random(3)).thenReturn(mathReturnValue);

        // Run
        MetaExpression result = RandomConstruct.process(value, math);

        // Verify
        verify(math, times(1)).random(3);

        // Assert
        Assert.assertEquals(result, ExpressionBuilderHelper.fromValue("Command"));
    }
}
