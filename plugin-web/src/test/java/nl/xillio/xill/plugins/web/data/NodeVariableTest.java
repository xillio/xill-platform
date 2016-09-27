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
package nl.xillio.xill.plugins.web.data;

import nl.xillio.xill.plugins.web.data.NodeVariable;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the {@link NodeVariable}.
 */
public class NodeVariableTest {

    /**
     * Test getTextPreview method.
     */
    @Test
    public void testNodeTextPreview() {
        // mock
        String data = "Some node content";

        WebElement element = mock(WebElement.class);
        when(element.getAttribute(anyString())).thenReturn(data);
        NodeVariable nodeVariable = new NodeVariable(null, element);

        // run
        String result = nodeVariable.getTextPreview();
        Assert.assertEquals(result, data);
    }
}

