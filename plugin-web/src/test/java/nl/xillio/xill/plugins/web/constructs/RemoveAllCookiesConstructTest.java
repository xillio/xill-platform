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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.data.PageVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Tests the {@link RemoveAllCookiesConstruct}.
 */
public class RemoveAllCookiesConstructTest extends TestUtils {

    /**
     * test the process with normal usage.
     *
     * @throws Exception
     */
    @Test
    public void testProcessNormalUsage() throws Exception {
        // mock
        WebService webService = mock(WebService.class);
        RemoveAllCookiesConstruct construct = new RemoveAllCookiesConstruct();
        construct.setWebService(webService);
        // The page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mockExpression(ATOMIC);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // run
        MetaExpression output = process(construct,page);

        // Wheter we parse the pageVariable only once
        verify(page, times(2)).getMeta(PageVariable.class);

        // We had one item to delete
        verify(webService, times(1)).deleteCookies(pageVariable);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process with null input given.
     */
    @Test
    public void testNullInput() {
        // mock
        WebService webService = mock(WebService.class);
        RemoveAllCookiesConstruct construct = new RemoveAllCookiesConstruct();
        construct.setWebService(webService);
        MetaExpression input = mockExpression(ATOMIC);
        when(input.isNull()).thenReturn(true);

        // run
        MetaExpression output = process(construct,input);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process when the webService breaks
     *
     * @throws Exception
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "breaking in webService")
    public void testProcessWhenWebServiceBreaks() throws Exception {
        // mock
        WebService webService = mock(WebService.class);
        RemoveAllCookiesConstruct construct = new RemoveAllCookiesConstruct();
        construct.setWebService(webService);

        // The page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mockExpression(ATOMIC);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // we make it break
        doThrow(new RobotRuntimeException("breaking in webService")).when(webService).deleteCookies(pageVariable);

        // run
        process(construct,page);
    }

    /**
     * Test the process when no page is given.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessNoPageGiven() {
        // mock
        WebService webService = mock(WebService.class);
        RemoveAllCookiesConstruct construct = new RemoveAllCookiesConstruct();
        construct.setWebService(webService);
        // The page
        MetaExpression page = mockExpression(ATOMIC);
        when(page.getMeta(PageVariable.class)).thenReturn(null);

        // run
        process(construct,page);
    }

}
