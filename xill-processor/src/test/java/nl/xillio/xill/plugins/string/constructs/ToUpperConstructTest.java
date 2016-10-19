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
import nl.xillio.xill.plugins.string.services.string.StringUtilityService;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Test the {@link ToUpperConstruct}.
 */
public class ToUpperConstructTest extends TestUtils {

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

        String returnValue = "TESTING";
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(stringService.toUpperCase(stringValue)).thenReturn(returnValue);

        ToUpperConstruct construct = new ToUpperConstruct(stringService);
        // Run
        MetaExpression result = process(construct, string);

        // Verify
        verify(stringService, times(1)).toUpperCase(stringValue);

        // Assert
        Assert.assertEquals(result.getStringValue(), returnValue);
    }

    /**
     * Test the process method when null is used.
     */
    @Test
    public void processNullUsage() {

        //Mock
        StringUtilityService stringService = mock(StringUtilityService.class);

        ToUpperConstruct construct = new ToUpperConstruct(stringService);
        //Run
        MetaExpression result = process(construct, NULL);

        // Assert
        Assert.assertEquals(result, NULL);
    }
}
