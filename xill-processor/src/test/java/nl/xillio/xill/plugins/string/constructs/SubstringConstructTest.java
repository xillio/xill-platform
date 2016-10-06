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
 * Test the {@link SubstringConstruct}.
 */
public class SubstringConstructTest extends TestUtils{

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

        int startValue = 1;
        MetaExpression start = mockExpression(ATOMIC);
        when(start.getNumberValue()).thenReturn(startValue);
        when(start.isNull()).thenReturn(false);

        int endValue = 3;
        MetaExpression end = mockExpression(ATOMIC);
        when(end.getNumberValue()).thenReturn(endValue);
        when(end.isNull()).thenReturn(false);

        String returnValue = "est";
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(stringService.subString(stringValue, startValue, endValue)).thenReturn(returnValue);

        SubstringConstruct construct = new SubstringConstruct(stringService);
        // Run
        MetaExpression result = process(construct, string, start, end);

        // Verify
        verify(stringService, times(1)).subString(stringValue, startValue, endValue);

        // Assert
        Assert.assertEquals(result.getStringValue(), returnValue);
    }

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processEndIs0() {
        // Mock
        String stringValue = "testing";
        MetaExpression string = mockExpression(ATOMIC);
        when(string.getStringValue()).thenReturn(stringValue);
        when(string.isNull()).thenReturn(false);

        int startValue = 1;
        MetaExpression start = mockExpression(ATOMIC);
        when(start.getNumberValue()).thenReturn(startValue);
        when(start.isNull()).thenReturn(false);

        int endValue = 0;
        MetaExpression end = mockExpression(ATOMIC);
        when(end.getNumberValue()).thenReturn(endValue);
        when(end.isNull()).thenReturn(false);

        String returnValue = stringValue;
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(stringService.subString(stringValue, startValue, stringValue.length())).thenReturn(returnValue);

        SubstringConstruct construct = new SubstringConstruct(stringService);
        // Run
        MetaExpression result = process(construct, string, start, end);

        // Verify
        verify(stringService, times(1)).subString(stringValue, startValue, stringValue.length());

        // Assert
        Assert.assertEquals(result.getStringValue(), returnValue);
    }

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processHighStartValue() {
        // Mock
        String stringValue = "testing";
        MetaExpression string = mockExpression(ATOMIC);
        when(string.getStringValue()).thenReturn(stringValue);
        when(string.isNull()).thenReturn(false);

        int startValue = 7;
        MetaExpression start = mockExpression(ATOMIC);
        when(start.getNumberValue()).thenReturn(startValue);
        when(start.isNull()).thenReturn(false);

        int endValue = 3;
        MetaExpression end = mockExpression(ATOMIC);
        when(end.getNumberValue()).thenReturn(endValue);
        when(end.isNull()).thenReturn(false);

        String returnValue = "est";
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(stringService.subString(stringValue, 0, endValue)).thenReturn(returnValue);

        SubstringConstruct construct = new SubstringConstruct(stringService);
        // Run
        MetaExpression result = process(construct, string, start, end);

        // Verify
        verify(stringService, times(1)).subString(stringValue, 0, endValue);

        // Assert
        Assert.assertEquals(result.getStringValue(), returnValue);
    }

    /**
     * Test the process method under normal circumstances.
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Index out of bounds: .*")
    public void processErrorThrown() {
        // Mock
        String stringValue = "testing";
        MetaExpression string = mockExpression(ATOMIC);
        when(string.getStringValue()).thenReturn(stringValue);
        when(string.isNull()).thenReturn(false);

        int startValue = 1;
        MetaExpression start = mockExpression(ATOMIC);
        when(start.getNumberValue()).thenReturn(startValue);
        when(start.isNull()).thenReturn(false);

        int endValue = 3;
        MetaExpression end = mockExpression(ATOMIC);
        when(end.getNumberValue()).thenReturn(endValue);
        when(end.isNull()).thenReturn(false);

        Exception returnValue = new StringIndexOutOfBoundsException();
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(stringService.subString(stringValue, startValue, endValue)).thenThrow(returnValue);

        SubstringConstruct construct = new SubstringConstruct(stringService);
        // Run
        process(construct, string, start, end);

        // Verify
        verify(stringService, times(1)).subString(stringValue, startValue, endValue);
    }
}
