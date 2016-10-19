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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.string.services.string.StringUtilityService;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Test the {@link StartsWithConstruct}.
 */
public class StartsWithConstructTest extends TestUtils {

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processNormalUsage() {
        // Mock
        String stringValue = "testing";
        MetaExpression string = mockExpression(ATOMIC);
        when(string.getStringValue()).thenReturn(stringValue);
        when(string.isNull()).thenReturn(false);

        String prefixValue = "test";
        MetaExpression prefix = mockExpression(ATOMIC);
        when(prefix.getStringValue()).thenReturn(prefixValue);
        when(prefix.isNull()).thenReturn(false);

        boolean returnValue = true;
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(stringService.startsWith(stringValue, prefixValue)).thenReturn(returnValue);

        StartsWithConstruct construct = new StartsWithConstruct(stringService);
        // Run
        MetaExpression result = process(construct, string, prefix);

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
        MetaExpression string = mockExpression(ATOMIC);
        when(string.getStringValue()).thenReturn(stringValue);
        when(string.isNull()).thenReturn(true);

        String prefixValue = "test";
        MetaExpression prefix = mockExpression(ATOMIC);
        when(prefix.getStringValue()).thenReturn(prefixValue);
        when(prefix.isNull()).thenReturn(true);

        StringUtilityService stringService = mock(StringUtilityService.class);

        StartsWithConstruct construct = new StartsWithConstruct(stringService);
        // Run
        process(construct, string, prefix);

    }
}
