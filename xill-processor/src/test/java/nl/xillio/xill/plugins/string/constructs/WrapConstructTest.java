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
import nl.xillio.xill.plugins.string.services.string.StringUtilityService;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Test the {@link WrapConstruct}.
 */
public class WrapConstructTest {

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processNormalUsage() {
        // Mock
        String stringValue = "testing";
        MetaExpression string = mock(MetaExpression.class);
        when(string.getStringValue()).thenReturn(stringValue);

        int wrapValue = 5;
        MetaExpression wrap = mock(MetaExpression.class);
        when(wrap.getNumberValue()).thenReturn(wrapValue);

        boolean wrapLongWordsValue = true;
        MetaExpression wrapLongWords = mock(MetaExpression.class);
        when(wrapLongWords.getBooleanValue()).thenReturn(wrapLongWordsValue);

        String returnValue = "testi \n ng";
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(stringService.wrap(stringValue, wrapValue, wrapLongWordsValue)).thenReturn(returnValue);
        // Run
        MetaExpression result = WrapConstruct.process(string, wrap, wrapLongWords, stringService);

        // Verify
        verify(stringService, times(1)).wrap(stringValue, wrapValue, wrapLongWordsValue);

        // Assert
        Assert.assertEquals(result.getStringValue(), returnValue);
    }
}
