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
package nl.xillio.xill.plugins.web.constructs;

import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.data.PageVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test the {@link RemoveCookieConstruct}.
 */
public class RemoveCookieConstructTest extends ExpressionBuilderHelper {

    /**
     * Test the process when a list of cookies to remove is handed.
     */
    @Test
    public void testProcessListUsage() {
        // mock
        WebService webService = mock(WebService.class);

        // The values the input contains
        MetaExpression first = mock(MetaExpression.class);
        MetaExpression second = mock(MetaExpression.class);
        when(first.getStringValue()).thenReturn("first cookie!");
        when(second.getStringValue()).thenReturn("second cookie...");
        List<MetaExpression> inputValue = Arrays.asList(first, second);

        // The page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mock(MetaExpression.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The cookie
        MetaExpression cookie = mock(MetaExpression.class);
        when(cookie.isNull()).thenReturn(false);
        when(cookie.getValue()).thenReturn(inputValue);
        when(cookie.getType()).thenReturn(LIST);

        // run
        MetaExpression output = RemoveCookieConstruct.process(page, cookie, webService);

        // verify

        // Wheter we ask for their values only once
        verify(first, times(1)).getStringValue();
        verify(second, times(1)).getStringValue();

        // Wheter we parse the pageVariable only once
        verify(page, times(2)).getMeta(PageVariable.class);

        // Wheter we parse the cookie only once
        verify(cookie, times(1)).isNull();
        verify(cookie, times(1)).getValue();
        verify(cookie, times(1)).getType();

        // We had two items to delete
        verify(webService, times(2)).deleteCookieNamed(eq(pageVariable), anyString());

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process with null page given.
     */
    @Test
    public void testNullPage() {
        // mock
        WebService webService = mock(WebService.class);
        MetaExpression input = mock(MetaExpression.class);
        MetaExpression cookie = mock(MetaExpression.class);
        when(input.isNull()).thenReturn(true);

        // run
        MetaExpression output = RemoveCookieConstruct.process(input, cookie, webService);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process with null page given.
     */
    @Test
    public void testNullCookie() {
        // mock
        WebService webService = mock(WebService.class);
        MetaExpression input = mock(MetaExpression.class);
        MetaExpression cookie = mock(MetaExpression.class);
        when(cookie.isNull()).thenReturn(true);

        // run
        MetaExpression output = RemoveCookieConstruct.process(input, cookie, webService);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process when a single cookie to delete is handed.
     */
    @Test
    public void testProcessAtomicUsage() {
        // mock
        WebService webService = mock(WebService.class);

        // The page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mock(MetaExpression.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The cookie
        MetaExpression cookie = mock(MetaExpression.class);
        when(cookie.isNull()).thenReturn(false);
        when(cookie.getStringValue()).thenReturn("deleteThisCookie");
        when(cookie.getType()).thenReturn(ATOMIC);

        // run
        MetaExpression output = RemoveCookieConstruct.process(page, cookie, webService);

        // Wheter we parse the pageVariable only once
        verify(page, times(2)).getMeta(PageVariable.class);

        // Wheter we parse the cookie only once
        verify(cookie, times(1)).isNull();
        verify(cookie, times(1)).getStringValue();
        verify(cookie, times(1)).getType();

        // We had one item to delete
        verify(webService, times(1)).deleteCookieNamed(eq(pageVariable), anyString());

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process when the webService breaks
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessWhenWebServiceBreaks() {
        // mock
        WebService webService = mock(WebService.class);

        // The page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mock(MetaExpression.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The cookie
        MetaExpression cookie = mock(MetaExpression.class);
        when(cookie.isNull()).thenReturn(false);
        when(cookie.getValue()).thenReturn("deleteThisCookie");
        when(cookie.getType()).thenReturn(ATOMIC);

        // we make it break
        doThrow(new RobotRuntimeException("Resistance is futile.")).when(webService).deleteCookieNamed(any(), anyString());

        RemoveCookieConstruct.process(page, cookie, webService);
    }

    /**
     * Test the process when no page is given.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessNoPageGiven() {
        // mock
        WebService webService = mock(WebService.class);

        // The page
        MetaExpression page = mock(MetaExpression.class);
        when(page.getMeta(PageVariable.class)).thenReturn(null);

        // The cookie
        MetaExpression cookie = mock(MetaExpression.class);

        // run
        RemoveCookieConstruct.process(page, cookie, webService);
    }
}
