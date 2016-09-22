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
import nl.xillio.xill.plugins.string.services.string.UrlUtilityServiceImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the {@link AbsoluteURLConstruct}.
 */
public class AbsoluteURLConstructTest extends TestUtils {
    private AbsoluteURLConstruct construct = new AbsoluteURLConstruct();

    @BeforeClass()
    private void injectDependencies() {
        construct.setUrlUtilityService(new UrlUtilityServiceImpl());
    }

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processNormalUsage() {
        // MetaExpressions.
        MetaExpression pageUrl = fromValue("http://www.xillio.nl/calendar/");
        MetaExpression relativeUrl = fromValue("../");

        // Run.
        MetaExpression result = this.process(construct, pageUrl, relativeUrl);

        // Assert.
        Assert.assertEquals(result.getStringValue(), "http://www.xillio.nl/");
    }

    /**
     * Tests the process when an empty relativeUrl is handed, in which case the last slash is deleted and the url is cleaned.
     */
    @Test
    public void processEmptyRelativeUrl() {
        // MetaExpressions.
        MetaExpression pageUrl = fromValue("http://www.xillio.nl/calendar/");
        MetaExpression relativeUrl = fromValue("");

        // Run.
        MetaExpression result = this.process(construct, pageUrl, relativeUrl);

        // Assert.
        Assert.assertEquals(result.getStringValue(), "http://www.xillio.nl/calendar");
    }

    /**
     * Test the process when the relative url reuses the protocol.
     */
    @Test
    public void processReuseProtocol() {
        // MetaExpressions.
        MetaExpression pageUrl = fromValue("https://xillio.nl/");
        MetaExpression relativeUrl = fromValue("//example.com");

        // Run.
        MetaExpression result = this.process(construct, pageUrl, relativeUrl);

        // Assert.
        Assert.assertEquals(result.getStringValue(), "https://example.com");
    }
}
