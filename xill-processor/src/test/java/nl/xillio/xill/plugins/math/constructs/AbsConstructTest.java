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
 * Test the {@link AbsConstruct}.
 */
public class AbsConstructTest {

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processNormalUsage() {
        // Mock
        Double numberValue = -504.0;
        MetaExpression value = mock(MetaExpression.class);
        when(value.getNumberValue()).thenReturn(numberValue);

        Double mathReturnValue = 504.0;
        MathOperations math = mock(MathOperations.class);
        when(math.abs(numberValue)).thenReturn(mathReturnValue);

        // Run
        MetaExpression result = AbsConstruct.process(value, math);

        // Verify
        verify(math, times(1)).abs(numberValue);

        // Assert
        Assert.assertEquals(result.getNumberValue().doubleValue(), mathReturnValue);
    }
}
