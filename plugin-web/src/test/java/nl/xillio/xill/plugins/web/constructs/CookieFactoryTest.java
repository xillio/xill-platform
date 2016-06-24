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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.data.CookieFactory;
import nl.xillio.xill.plugins.web.data.WebVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Test the {@link CookieFactory}
 */
public class CookieFactoryTest {

    /**
     * Test the construct when expected values are given.
     */
    @Test
    public void testProcessNormalUsage() {
        // mock
        WebVariable webVariable = mock(WebVariable.class);
        WebService webService = mock(WebService.class);

        Map<String, MetaExpression> cookie = new HashMap<>();
        cookie.put("name", fromValue("A name"));
        cookie.put("domain", fromValue("A domain"));
        cookie.put("path", fromValue("A path"));
        cookie.put("value", fromValue("A value"));
        cookie.put("expires", fromValue("1995-02-03T12:12:12"));
        CookieFactory factory = new CookieFactory();

        // run
        factory.setCookie(webVariable, cookie, webService);
    }

    /**
     * Test the construct when expected values are given, but the metaExpressions in the cookiesettings evalueate to "null".
     */
    @Test
    public void testProcessNullStringsInInput() {
        // mock
        WebVariable webVariable = mock(WebVariable.class);
        WebService webService = mock(WebService.class);

        Map<String, MetaExpression> cookie = new HashMap<>();
        cookie.put("name", fromValue("A name"));
        cookie.put("domain", fromValue("null"));
        cookie.put("path", fromValue("null"));
        cookie.put("value", fromValue("null"));
        cookie.put("expires", fromValue("null"));
        CookieFactory factory = new CookieFactory();

        // run
        factory.setCookie(webVariable, cookie, webService);
    }

    /**
     * Test the construct when no values are given but the name value.
     */
    @Test
    public void testProcessNoInputGivenButName() {
        // mock
        WebVariable webVariable = mock(WebVariable.class);
        WebService webService = mock(WebService.class);

        Map<String, MetaExpression> cookie = new HashMap<>();
        cookie.put("name", fromValue("A name"));
        CookieFactory factory = new CookieFactory();

        // run
        factory.setCookie(webVariable, cookie, webService);
    }

    /**
     * Test the construct when expected values are given, but the metaExpressions in the cookiesettings evalueate to "null".
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Invalid cookie. Atribute \\'expires\\' does not have the format yyyy-MM-ddThh:mm:ss")
    public void testInvalidDateFormatForExpirationDate() {
        // mock
        WebVariable webVariable = mock(WebVariable.class);
        WebService webService = mock(WebService.class);

        Map<String, MetaExpression> cookie = new HashMap<>();
        cookie.put("name", fromValue("A name"));
        cookie.put("expires", fromValue("Invalid date format"));
        CookieFactory factory = new CookieFactory();

        // run
        factory.setCookie(webVariable, cookie, webService);
    }

    /**
     * Test the construct when no cookie input is given.
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Invalid cookie. Attribute \\'name\\' is empty.")
    public void testProcessWithNoInput() {
        // mock
        WebVariable webVariable = mock(WebVariable.class);
        WebService webService = mock(WebService.class);

        Map<String, MetaExpression> cookie = new HashMap<>();
        CookieFactory factory = new CookieFactory();

        // run
        factory.setCookie(webVariable, cookie, webService);
    }

    /**
     * Test the process when we fail to adda cookie.
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to add cookie: cookieName")
    public void testProcessWhenWeFailToAddCookie() {
        // mock
        WebVariable webVariable = mock(WebVariable.class);
        WebService webService = mock(WebService.class);

        Map<String, MetaExpression> cookie = new HashMap<>();
        cookie.put("name", fromValue("cookieName"));
        CookieFactory factory = new CookieFactory();

        doThrow(new RobotRuntimeException("I crashed!")).when(webService).addCookie(any(), any());

        // run
        factory.setCookie(webVariable, cookie, webService);
    }

}
