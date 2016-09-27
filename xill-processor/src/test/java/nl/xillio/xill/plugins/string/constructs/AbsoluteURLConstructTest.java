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
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.string.services.string.UrlUtilityService;
import nl.xillio.xill.plugins.string.services.string.UrlUtilityServiceImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Test the {@link AbsoluteURLConstruct}.
 */
public class AbsoluteURLConstructTest extends TestUtils {
    private UrlUtilityService urlUtilityService;
    private AbsoluteURLConstruct construct;

    /**
     * Reset the construct and spied service before each method.
     */
    @BeforeMethod
    private void resetConstruct() {
        urlUtilityService = spy(new UrlUtilityServiceImpl());
        construct = new AbsoluteURLConstruct(urlUtilityService);
    }

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processNormalUsage() {
        // MetaExpressions.
        MetaExpression pageUrl = fromValue("www.xillio.nl/calendar/");
        MetaExpression relativeUrl = fromValue("../");

        // Run.
        MetaExpression result = this.process(construct, pageUrl, relativeUrl);

        // Verify.
        verify(urlUtilityService, times(1)).tryConvert("http://" + pageUrl.getStringValue(), relativeUrl.getStringValue());

        // Assert.
        Assert.assertEquals(result.getStringValue(), "http://www.xillio.nl/");
    }

    @Test
    public void processDifferentProtocol(){
        // MetaExpressions.
        MetaExpression pageUrl = fromValue("xill://robot.io/");
        MetaExpression relativeUrl = fromValue("run");

        // Run.
        MetaExpression result = this.process(construct, pageUrl, relativeUrl);

        // Verify.
        verify(urlUtilityService, times(1)).tryConvert(pageUrl.getStringValue(), relativeUrl.getStringValue());

        // Assert.
        Assert.assertEquals(result.getStringValue(), "xill://robot.io/run");
    }

    /**
     * Tests the process when an empty relativeUrl is handed, in which case the last slash is deleted and the url is cleaned.
     */
    @Test
    public void processEmptyRelativeUrl() {
        // MetaExpressions.
        MetaExpression pageUrl = fromValue("http://www.xillio.nl/calendar/");
        MetaExpression relativeUrl = fromValue("");
        String urlReturnValue = "http://www.xillio.nl/calendar";

        // Run.
        MetaExpression result = this.process(construct, pageUrl, relativeUrl);

        // Verify.
        verify(urlUtilityService, times(0)).tryConvert(any(), any());
        verify(urlUtilityService, times(1)).cleanupUrl(urlReturnValue);

        // Assert.
        Assert.assertEquals(result.getStringValue(), urlReturnValue);
    }

    /**
     * Tests the process when an empty relativeUrl is handed and the page url has no trailing slash, in which case the url is just cleaned.
     */
    @Test
    public void processEmptyRelativeUrlNoTrailingSlash() {
        // MetaExpressions.
        MetaExpression pageUrl = fromValue("http://www.xillio.nl/calendar");
        MetaExpression relativeUrl = fromValue("");

        // Run.
        MetaExpression result = this.process(construct, pageUrl, relativeUrl);

        // Verify.
        verify(urlUtilityService, times(0)).tryConvert(any(), any());
        verify(urlUtilityService, times(1)).cleanupUrl(pageUrl.getStringValue());

        // Assert.
        Assert.assertEquals(result.getStringValue(), pageUrl.getStringValue());
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

        // Verify.
        verify(urlUtilityService, times(1)).getProtocol(pageUrl.getStringValue());

        // Assert.
        Assert.assertEquals(result.getStringValue(), "https://example.com");
    }

    /**
     * Tests the process when it fails to return a value.
     */
    @Test(expectedExceptions = OperationFailedException.class, expectedExceptionsMessageRegExp = "Could not convert relative URL to absolute URL..*The page url parameter is invalid. Pass correct page url.")
    public void processFailureToConvert() {
        // MetaExpressions.
        MetaExpression pageUrl = fromValue("http://www.xillio.nl/calendar/");
        MetaExpression relativeUrl = fromValue("../");

        // Mock.
        UrlUtilityService url = mock(UrlUtilityService.class);
        when(url.tryConvert(pageUrl.getStringValue(), relativeUrl.getStringValue())).thenReturn(null);

        // Run.
        AbsoluteURLConstruct constructWithMock = new AbsoluteURLConstruct(url);
        this.process(constructWithMock, pageUrl, relativeUrl);

        // Verify.
        verify(url, times(1)).tryConvert(pageUrl.getStringValue(), relativeUrl.getStringValue());
    }

    /**
     * Tests the process when it fails to return a value.
     */
    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Illegal argument was handed to the matcher when trying to convert the URL..*")
    public void processErrorOnConvert() {
        // MetaExpressions.
        MetaExpression pageUrl = fromValue("http://www.xillio.nl/calendar/");
        MetaExpression relativeUrl = fromValue("../");

        // Mock.
        UrlUtilityService url = mock(UrlUtilityService.class);
        when(url.tryConvert(pageUrl.getStringValue(), relativeUrl.getStringValue())).thenThrow(new IllegalArgumentException());

        // Run.
        AbsoluteURLConstruct constructWithMock = new AbsoluteURLConstruct(url);
        this.process(constructWithMock, pageUrl, relativeUrl);

        // Verify.
        verify(url, times(1)).tryConvert(pageUrl.getStringValue(), relativeUrl.getStringValue());
    }
}
