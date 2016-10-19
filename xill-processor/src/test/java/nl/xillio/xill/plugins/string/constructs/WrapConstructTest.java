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
 * Test the {@link WrapConstruct}.
 */
public class WrapConstructTest extends TestUtils {

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processNormalUsage() {
        // Mock
        String stringValue = "testing";
        MetaExpression string = mockExpression(ATOMIC);
        when(string.getStringValue()).thenReturn(stringValue);

        int wrapValue = 5;
        MetaExpression wrap = mockExpression(ATOMIC);
        when(wrap.getNumberValue()).thenReturn(wrapValue);

        boolean wrapLongWordsValue = true;
        MetaExpression wrapLongWords = mockExpression(ATOMIC);
        when(wrapLongWords.getBooleanValue()).thenReturn(wrapLongWordsValue);

        String returnValue = "testi \n ng";
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(stringService.wrap(stringValue, wrapValue, wrapLongWordsValue)).thenReturn(returnValue);

        WrapConstruct construct = new WrapConstruct(stringService);
        // Run
        MetaExpression result = process(construct, string, wrap, wrapLongWords);

        // Verify
        verify(stringService, times(1)).wrap(stringValue, wrapValue, wrapLongWordsValue);

        // Assert
        Assert.assertEquals(result.getStringValue(), returnValue);
    }
}
