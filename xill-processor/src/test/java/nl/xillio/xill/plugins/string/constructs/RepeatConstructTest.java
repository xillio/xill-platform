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
 * Test the {@link RepeatConstruct}.
 */
public class RepeatConstructTest extends TestUtils {

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processNormalUsage() {
        // Mock
        String stringValue = "eat, sleep, rave, repeat. ";
        MetaExpression string = mockExpression(ATOMIC);
        when(string.getStringValue()).thenReturn(stringValue);

        int repeatValue = 4;
        MetaExpression repeat = mockExpression(ATOMIC);
        when(repeat.getNumberValue()).thenReturn(repeatValue);

        String returnValue = "eat, sleep, rave, repeat. eat, sleep, rave, repeat. eat, sleep, rave, repeat. eat, sleep rave, repeat. ";
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(stringService.repeat(stringValue, repeatValue)).thenReturn(returnValue);

        RepeatConstruct construct = new RepeatConstruct(stringService);
        // Run
        MetaExpression result = process(construct, string, repeat);

        // Verify
        verify(stringService, times(1)).repeat(stringValue, repeatValue);

        // Assert
        Assert.assertEquals(result.getStringValue(), returnValue);
    }
}
