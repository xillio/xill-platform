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
 * Test the {@link ContainsConstruct}.
 */
public class ContainsConstructTest extends TestUtils{

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processNormalUsage() {
        // Mock
        String parentValue = "testing";
        MetaExpression parent = mockExpression(ATOMIC);
        when(parent.getStringValue()).thenReturn(parentValue);

        String childValue = "ing";
        MetaExpression child = mockExpression(ATOMIC);
        when(child.getStringValue()).thenReturn(childValue);

        boolean returnValue = true;
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(stringService.contains(parentValue, childValue)).thenReturn(returnValue);

        ContainsConstruct construct = new ContainsConstruct(stringService);
        // Run
        MetaExpression result = process(construct, parent, child);

        // Verify
        verify(stringService, times(1)).contains(parentValue, childValue);

        // Assert
        Assert.assertEquals(result.getBooleanValue(), returnValue);
    }
}
