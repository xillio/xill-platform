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

import com.google.common.collect.Sets;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.data.NodeVariable;
import nl.xillio.xill.plugins.web.data.PageVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.openqa.selenium.Cookie;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

import static org.mockito.Mockito.*;

/**
 * Test the {@link PageInfoConstruct}.
 */
public class PageInfoConstructTest extends TestUtils {

    /**
     * Tests the construct under normal circumstances
     */
    @Test
    public void testProcessNormalUsage() {
        // mock
        WebService webService = mock(WebService.class);
        PageInfoConstruct construct = new PageInfoConstruct();
        construct.setWebService(webService);
        // The page
        MetaExpression input = mockExpression(ATOMIC);
        PageVariable pageVariable = mock(PageVariable.class);
        when(input.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The cookie extracted
        Cookie first = mock(Cookie.class);
        when(webService.getName(first)).thenReturn("first");
        when(webService.getDomain(first)).thenReturn("first domain");
        when(webService.getPath(first)).thenReturn("first path");
        when(webService.getValue(first)).thenReturn("first value");

        // the process
        when(webService.getCookies(pageVariable)).thenReturn(Sets.newHashSet(first));

        // runs
        MetaExpression output = process(construct, input);

        // verify
        verify(input, times(2)).getMeta(PageVariable.class);
        verify(webService, times(2)).getName(first);
        verify(webService, times(1)).getDomain(first);
        verify(webService, times(1)).getPath(first);
        verify(webService, times(1)).getValue(first);

        // assert
        Assert.assertEquals(output.getType(), OBJECT);
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, MetaExpression> result = (LinkedHashMap<String, MetaExpression>) output.getValue();
        Assert.assertEquals(result.getOrDefault("cookies", NULL).getType(), OBJECT);
    }

    /**
     * Test the process with null input given.
     */
    @Test
    public void testNullInput() {
        // mock
        WebService webService = mock(WebService.class);
        PageInfoConstruct construct = new PageInfoConstruct();
        construct.setWebService(webService);
        MetaExpression input = mockExpression(ATOMIC);
        when(input.isNull()).thenReturn(true);

        // run
        MetaExpression output = process(construct, input);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process when no node was in the expression.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessNoNodeGiven() {
        // mock
        WebService webService = mock(WebService.class);
        PageInfoConstruct construct = new PageInfoConstruct();
        construct.setWebService(webService);
        // The input
        MetaExpression input = mockExpression(ATOMIC);

        when(input.getMeta(NodeVariable.class)).thenReturn(null);

        // run
        process(construct, input);
    }

    /**
     * Test the process when selenium breaks.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessWhenItBreaks() {
        // mock
        WebService webService = mock(WebService.class);
        PageInfoConstruct construct = new PageInfoConstruct();
        construct.setWebService(webService);
        // The input
        MetaExpression input = mockExpression(ATOMIC);
        PageVariable pageVariable = mock(PageVariable.class);
        when(input.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The cookie extracted
        Cookie first = mock(Cookie.class);
        when(webService.getName(first)).thenThrow(new RobotRuntimeException("We throw"));
        when(webService.getDomain(first)).thenReturn("first domain");
        when(webService.getPath(first)).thenReturn("first path");
        when(webService.getValue(first)).thenReturn("first value");

        // the process
        when(webService.getCookies(pageVariable)).thenReturn(Sets.newHashSet(first));

        // runs
        process(construct, input);
    }
}
