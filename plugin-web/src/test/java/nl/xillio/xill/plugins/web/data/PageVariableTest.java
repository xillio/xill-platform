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

import nl.xillio.xill.plugins.web.data.PageVariable;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the {@link PageVariable}.
 */
public class PageVariableTest {

    /**
     * Test getTextPreview method.
     */
    @Test
    public void testPageTextPreview() {
        // mock
        String data = "Some page content";

        WebDriver driver = mock(WebDriver.class);
        when(driver.getPageSource()).thenReturn(data);
        PageVariable pageVariable = new PageVariable(driver, null);

        // run
        String result = pageVariable.getTextPreview();
        Assert.assertEquals(result, data);
    }
}

