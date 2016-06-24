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
package nl.xillio.xill.plugins.string.constructs;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.string.services.string.StringUtilityService;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Test the {@link StartsWithConstruct}.
 */
public class StartsWithConstructTest {

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processNormalUsage() {
        // Mock
        String stringValue = "testing";
        MetaExpression string = mock(MetaExpression.class);
        when(string.getStringValue()).thenReturn(stringValue);
        when(string.isNull()).thenReturn(false);

        String prefixValue = "test";
        MetaExpression prefix = mock(MetaExpression.class);
        when(prefix.getStringValue()).thenReturn(prefixValue);
        when(prefix.isNull()).thenReturn(false);

        boolean returnValue = true;
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(stringService.startsWith(stringValue, prefixValue)).thenReturn(returnValue);
        // Run
        MetaExpression result = StartsWithConstruct.process(string, prefix, stringService);

        // Verify
        verify(stringService, times(1)).startsWith(stringValue, prefixValue);

        // Assert
        Assert.assertEquals(result.getBooleanValue(), returnValue);
    }

    /**
     * Test the process when the given values are null.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void processNullValueGiven() {
        // Mock
        String stringValue = "testing";
        MetaExpression string = mock(MetaExpression.class);
        when(string.getStringValue()).thenReturn(stringValue);
        when(string.isNull()).thenReturn(true);

        String prefixValue = "test";
        MetaExpression prefix = mock(MetaExpression.class);
        when(prefix.getStringValue()).thenReturn(prefixValue);
        when(prefix.isNull()).thenReturn(true);

        StringUtilityService stringService = mock(StringUtilityService.class);

        StartsWithConstruct.process(string, prefix, stringService);

    }
}
