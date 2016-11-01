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
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.plugins.string.services.string.StringUtilityService;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Test the {@link JoinConstruct}.
 */
public class JoinConstructTest extends TestUtils {

    /**
     * Test the process method with an ATOMIC value given.
     */
    @Test
    public void processAtomicInput() {
        // Mock
        String valueValue = "CORRECT";
        MetaExpression value = mockExpression(ATOMIC);
        when(value.getStringValue()).thenReturn(valueValue);
        when(value.getType()).thenReturn(ATOMIC);

        String delimiterValue = "a";
        MetaExpression delimiter = mockExpression(ATOMIC);
        when(delimiter.getStringValue()).thenReturn(delimiterValue);

        String returnValue = "CORRECT";
        StringUtilityService stringService = mock(StringUtilityService.class);

        JoinConstruct construct = new JoinConstruct(stringService);
        // Run
        MetaExpression result = process(construct, value, delimiter);

        // Verify
        verify(stringService, times(0)).join(any(), any());

        // Assert
        Assert.assertEquals(result.getStringValue(), returnValue);
    }

    /**
     * Test the process method with a LIST value given.
     */
    @Test
    public void processListInput() {
        // Mock
        String firstValue = "CORRECT";
        MetaExpression first = mockExpression(ATOMIC);
        when(first.getStringValue()).thenReturn(firstValue);

        String secondValue = "MONSIEUR";
        MetaExpression second = mockExpression(ATOMIC);
        when(second.getStringValue()).thenReturn(secondValue);

        List<MetaExpression> listValue = Arrays.asList(first, second);
        String[] listValueAsStrings = new String[]{firstValue, secondValue};
        MetaExpression list = mockExpression(LIST);
        when(list.getValue()).thenReturn(listValue);
        when(list.getType()).thenReturn(LIST);

        String delimiterValue = "";
        MetaExpression delimiter = mockExpression(ATOMIC);
        when(delimiter.getStringValue()).thenReturn(delimiterValue);

        String returnValue = "CORRECTMONSIEUR";
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(stringService.join(listValueAsStrings, delimiterValue)).thenReturn(returnValue);

        JoinConstruct construct = new JoinConstruct(stringService);
        // Run
        MetaExpression result = process(construct, list, delimiter);

        // Verify
        verify(stringService, times(1)).join(listValueAsStrings, delimiterValue);

        // Assert
        Assert.assertEquals(result.getStringValue(), returnValue);
    }

    /**
     * Test the process method with a OBJECT value given.
     */
    @Test
    public void processObjectInput() {
        // Mock
        String firstValue = "CORRECT";
        MetaExpression first = mockExpression(ATOMIC);
        when(first.getStringValue()).thenReturn(firstValue);

        String secondValue = "MONSIEUR";
        MetaExpression second = mockExpression(ATOMIC);
        when(second.getStringValue()).thenReturn(secondValue);

        Map<String, MetaExpression> objectValue = new LinkedHashMap<String, MetaExpression>();
        objectValue.put("first", first);
        objectValue.put("second", second);
        MetaExpression object = mockExpression(OBJECT);
        when(object.getValue()).thenReturn(objectValue);
        when(object.getType()).thenReturn(OBJECT);

        String[] listValueAsStrings = new String[]{firstValue, secondValue};

        String delimiterValue = "";
        MetaExpression delimiter = mockExpression(ATOMIC);
        when(delimiter.getStringValue()).thenReturn(delimiterValue);

        String returnValue = "CORRECTMONSIEUR";
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(stringService.join(listValueAsStrings, delimiterValue)).thenReturn(returnValue);

        JoinConstruct construct = new JoinConstruct(stringService);
        // Run
        MetaExpression result = process(construct, object, delimiter);

        // Verify
        verify(stringService, times(1)).join(listValueAsStrings, delimiterValue);

        // Assert
        Assert.assertEquals(result.getStringValue(), returnValue);
    }
}
