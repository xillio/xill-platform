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
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.string.services.string.UrlUtilityService;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Test the {@link AbsoluteURLConstruct}.
 */
public class AbsoluteURLConstructTest {

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processNormalUsage() {
        // Mock
        String pageUrlValue = "http://www.xillio.nl/calendar/";
        MetaExpression pageUrl = mock(MetaExpression.class);
        when(pageUrl.getStringValue()).thenReturn(pageUrlValue);

        String relativeUrlValue = "../";
        MetaExpression relativeUrl = mock(MetaExpression.class);
        when(relativeUrl.getStringValue()).thenReturn(relativeUrlValue);

        String UrlReturnValue = "http://www.xillio.nl/";
        UrlUtilityService url = mock(UrlUtilityService.class);
        when(url.tryConvert(pageUrlValue, relativeUrlValue)).thenReturn(UrlReturnValue);

        // Run
        MetaExpression result = AbsoluteURLConstruct.process(pageUrl, relativeUrl, url);

        // Verify
        verify(url, times(1)).tryConvert(pageUrlValue, relativeUrlValue);

        // Assert
        Assert.assertEquals(result.getStringValue(), UrlReturnValue);
    }

    /**
     * Tests the process when an empty relativeUrl is handed, in which case the last slash is deleted and the url is cleaned.
     */
    @Test
    public void processEmptyRelativeUrl() {
        // Mock
        String pageUrlValue = "http://www.xillio.nl/calendar/";
        MetaExpression pageUrl = mock(MetaExpression.class);
        when(pageUrl.getStringValue()).thenReturn(pageUrlValue);

        String relativeUrlValue = "";
        MetaExpression relativeUrl = mock(MetaExpression.class);
        when(relativeUrl.getStringValue()).thenReturn(relativeUrlValue);

        String UrlReturnValue = "http://www.xillio.nl/calendar";
        UrlUtilityService url = mock(UrlUtilityService.class);
        when(url.tryConvert(pageUrlValue, relativeUrlValue)).thenReturn(UrlReturnValue);
        when(url.cleanupUrl(UrlReturnValue)).thenReturn(UrlReturnValue);

        // Run
        MetaExpression result = AbsoluteURLConstruct.process(pageUrl, relativeUrl, url);

        // Verify
        verify(url, times(0)).tryConvert(any(), any());
        verify(url, times(1)).cleanupUrl(UrlReturnValue);

        // Assert
        Assert.assertEquals(result.getStringValue(), UrlReturnValue);
    }

    /**
     * Tests the process when it fails to return a value.
     */
    @Test(expectedExceptions = OperationFailedException.class, expectedExceptionsMessageRegExp = "Could not convert relative URL to absolute URL..*The page url parameter is invalid. Pass correct page url.")
    public void processFailureToConvert() {
        // Mock
        String pageUrlValue = "http://www.xillio.nl/calendar/";
        MetaExpression pageUrl = mock(MetaExpression.class);
        when(pageUrl.getStringValue()).thenReturn(pageUrlValue);

        String relativeUrlValue = "../";
        MetaExpression relativeUrl = mock(MetaExpression.class);
        when(relativeUrl.getStringValue()).thenReturn(relativeUrlValue);

        UrlUtilityService url = mock(UrlUtilityService.class);
        when(url.tryConvert(pageUrlValue, relativeUrlValue)).thenReturn(null);

        // Run
        AbsoluteURLConstruct.process(pageUrl, relativeUrl, url);

        // Verify
        verify(url, times(1)).tryConvert(pageUrlValue, relativeUrlValue);
    }

    /**
     * Tests the process when it fails to return a value.
     */
    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Illegal argument was handed to the matcher when trying to convert the URL..*")
    public void processErrorOnConvert() {
        // Mock
        String pageUrlValue = "http://www.xillio.nl/calendar/";
        MetaExpression pageUrl = mock(MetaExpression.class);
        when(pageUrl.getStringValue()).thenReturn(pageUrlValue);

        String relativeUrlValue = "../";
        MetaExpression relativeUrl = mock(MetaExpression.class);
        when(relativeUrl.getStringValue()).thenReturn(relativeUrlValue);

        UrlUtilityService url = mock(UrlUtilityService.class);
        when(url.tryConvert(pageUrlValue, relativeUrlValue)).thenThrow(new IllegalArgumentException());

        // Run
        AbsoluteURLConstruct.process(pageUrl, relativeUrl, url);

        // Verify
        verify(url, times(1)).tryConvert(pageUrlValue, relativeUrlValue);
    }
}
