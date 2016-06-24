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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.math.services.math.MathOperations;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Test the {@link RoundConstruct}.
 */
public class RoundConstructTest {

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processNormalUsage() {
        // Mock
        Double numberValue = 42.2;
        MetaExpression value = mock(MetaExpression.class);
        when(value.getNumberValue()).thenReturn(numberValue);

        long mathReturnValue = 42;
        MathOperations math = mock(MathOperations.class);
        when(math.round(numberValue)).thenReturn(mathReturnValue);

        // Run
        MetaExpression result = RoundConstruct.process(value, math);

        // Verify
        verify(math, times(1)).round(numberValue);

        // Assert
        Assert.assertEquals(result.getNumberValue().longValue(), mathReturnValue);
    }
}
