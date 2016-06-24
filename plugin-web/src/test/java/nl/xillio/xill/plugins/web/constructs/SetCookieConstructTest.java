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
import nl.xillio.xill.plugins.web.data.CookieFactory;
import nl.xillio.xill.plugins.web.data.PageVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Test the {@link SetCookieConstruct}.
 */
public class SetCookieConstructTest extends ExpressionBuilderHelper {

    /**
     * test the process with a list handed
     */
    @Test
    public void testNormalObjectUsage() {
        // mock
        WebService webService = mock(WebService.class);
        CookieFactory factory = mock(CookieFactory.class);

        // the page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mock(MetaExpression.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // the cookie
        LinkedHashMap<String, MetaExpression> cookieContent = new LinkedHashMap<>();
        MetaExpression cookie = mock(MetaExpression.class);
        MetaExpression cookieValue = mock(MetaExpression.class);
        cookieContent.put("content", cookieValue);
        when(cookie.getType()).thenReturn(OBJECT);
        when(cookie.getValue()).thenReturn(cookieContent);

        // run
        MetaExpression output = SetCookieConstruct.process(page, cookie, factory, webService);

        // verify
        verify(page, times(2)).getMeta(PageVariable.class);
        verify(cookie, times(1)).getValue();
        verify(factory, times(1)).setCookie(pageVariable, cookieContent, webService);

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
        CookieFactory factory = mock(CookieFactory.class);
        MetaExpression page = mock(MetaExpression.class);
        MetaExpression cookie = mock(MetaExpression.class);
        when(page.isNull()).thenReturn(true);

        // run
        MetaExpression output = SetCookieConstruct.process(page, cookie, factory, webService);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process with null cookie given
     */
    @Test
    public void testNullCookie() {
        // mock
        WebService webService = mock(WebService.class);
        CookieFactory factory = mock(CookieFactory.class);
        MetaExpression page = mock(MetaExpression.class);
        MetaExpression cookie = mock(MetaExpression.class);
        when(cookie.isNull()).thenReturn(true);

        // run
        MetaExpression output = SetCookieConstruct.process(page, cookie, factory, webService);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process with a list handed
     */
    @Test
    public void testNormalListUsage() {
        // mock
        WebService webService = mock(WebService.class);
        CookieFactory factory = mock(CookieFactory.class);

        // the page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mock(MetaExpression.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // the cookies
        List<MetaExpression> cookiesContent = new ArrayList<>();
        MetaExpression cookies = mock(MetaExpression.class);
        when(cookies.getType()).thenReturn(LIST);
        when(cookies.getValue()).thenReturn(cookiesContent);

        // the first cookie
        LinkedHashMap<String, MetaExpression> cookieContent = new LinkedHashMap<>();
        MetaExpression firstCookie = mock(MetaExpression.class);
        cookieContent.put("content", firstCookie);
        when(firstCookie.getType()).thenReturn(OBJECT);
        when(firstCookie.getValue()).thenReturn(cookieContent);
        cookiesContent.add(firstCookie);

        // run
        MetaExpression output = SetCookieConstruct.process(page, cookies, factory, webService);

        // verify
        verify(page, times(2)).getMeta(PageVariable.class);
        verify(cookies, times(1)).getType();
        verify(cookies, times(1)).getValue();
        verify(factory, times(1)).setCookie(pageVariable, cookieContent, webService);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process with a list handed with no objects in it.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testListWithNoObject() {
        // mock
        WebService webService = mock(WebService.class);
        CookieFactory factory = mock(CookieFactory.class);

        // the page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mock(MetaExpression.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // the cookies
        List<MetaExpression> cookiesContent = new ArrayList<>();
        MetaExpression cookies = mock(MetaExpression.class);
        when(cookies.getType()).thenReturn(LIST);
        when(cookies.getValue()).thenReturn(cookiesContent);

        // the first cookie
        LinkedHashMap<String, MetaExpression> cookieContent = new LinkedHashMap<>();
        MetaExpression firstCookie = mock(MetaExpression.class);
        cookieContent.put("content", firstCookie);
        when(firstCookie.getType()).thenReturn(LIST);
        when(firstCookie.getValue()).thenReturn(cookieContent);
        cookiesContent.add(firstCookie);

        // run
        SetCookieConstruct.process(page, cookies, factory, webService);
    }

    /**
     * Test the process with an invalid page handed.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testNoPageGiven() {
        // mock
        WebService webService = mock(WebService.class);
        CookieFactory factory = mock(CookieFactory.class);

        // the page
        MetaExpression page = mock(MetaExpression.class);
        MetaExpression cookies = mock(MetaExpression.class);

        // run
        SetCookieConstruct.process(page, cookies, factory, webService);
    }
}
