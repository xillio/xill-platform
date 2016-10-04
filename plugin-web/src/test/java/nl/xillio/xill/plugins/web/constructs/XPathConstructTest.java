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
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.data.NodeVariable;
import nl.xillio.xill.plugins.web.data.PageVariable;
import nl.xillio.xill.plugins.web.data.WebVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test the {@link XPathConstruct}
 */
public class XPathConstructTest extends TestUtils {

    /**
     * tests the process under normal circumstances with a page given.
     * We have a text query and findElements only returns one item.
     */
    @Test
    public void testNormalUsageWithPageAndTextQueryAndOneElement() {
        // mock
        WebService webService = mock(WebService.class);
        XPathConstruct construct = new XPathConstruct();
        construct.setWebService(webService);

        // The page
        MetaExpression page = mockExpression(ATOMIC);
        PageVariable pageVariable = mock(PageVariable.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The xpath
        String xPathValue = "xpath/text()";
        MetaExpression xpath = mockExpression(ATOMIC);
        when(xpath.getStringValue()).thenReturn(xPathValue);

        // The WebElement
        WebVariable element = mock(WebVariable.class);
        String elementText = "the text in the attribute of the element";

        // process SEL node
        when(webService.findElementsWithXpath(eq(pageVariable), anyString())).thenReturn(Collections.singletonList(element));
        when(webService.getAttribute(element, "innerHTML")).thenReturn(elementText);

        // run
        MetaExpression output = process(construct, page, xpath);

        // verify
        verify(webService, times(1)).getAttribute(eq(element), anyString());

        // assert
        Assert.assertEquals(output.getStringValue(), elementText);
    }

    /**
     * tests the process under normal circumstances with a node given.
     * We have a text query and findElements returns more items.
     */
    @Test
    public void testNormalUsageWithNodeAndTextQueryAndMoreElements() {
        // mock
        WebService webService = mock(WebService.class);
        XPathConstruct construct = new XPathConstruct();
        construct.setWebService(webService);

        // The node
        NodeVariable nodeVariable = mock(NodeVariable.class);
        MetaExpression node = mockExpression(ATOMIC);
        when(node.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // The xpath
        String xPathValue = "xpath/text()";
        MetaExpression xpath = mockExpression(ATOMIC);
        when(xpath.getStringValue()).thenReturn(xPathValue);

        // The WebElements
        WebVariable element1 = mock(WebVariable.class);
        String element1Text = "element1";
        WebVariable element2 = mock(WebVariable.class);
        String element2Text = "element2";

        // process SEL node
        when(webService.findElementsWithXpath(eq(nodeVariable), anyString())).thenReturn(Arrays.asList(element1, element2));
        when(webService.getAttribute(element1, "innerHTML")).thenReturn(element1Text);
        when(webService.getAttribute(element2, "innerHTML")).thenReturn(element2Text);

        // run
        MetaExpression output = process(construct, node, xpath);

        // verify
        verify(webService, times(2)).getAttribute(any(), anyString());

        // assert
        Assert.assertEquals(output.getType(), LIST);
        @SuppressWarnings("unchecked")
        List<MetaExpression> result = (List<MetaExpression>) output.getValue();
        Assert.assertEquals(result.get(0).getStringValue(), element1Text);
        Assert.assertEquals(result.get(1).getStringValue(), element2Text);
    }

    /**
     * tests the process under normal circumstances with a page given.
     * We have a text query and findElements returns more items.
     */
    @Test
    public void testNormalUsageWithPageAndTextQueryAndMoreElements() {
        // mock
        WebService webService = mock(WebService.class);
        XPathConstruct construct = new XPathConstruct();
        construct.setWebService(webService);

        // The page
        MetaExpression page = mockExpression(ATOMIC);
        PageVariable pageVariable = mock(PageVariable.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The xpath
        String xPathValue = "xpath/text()";
        MetaExpression xpath = mockExpression(ATOMIC);
        when(xpath.getStringValue()).thenReturn(xPathValue);

        // The WebElements
        WebVariable element1 = mock(WebVariable.class);
        String element1Text = "element1";
        WebVariable element2 = mock(WebVariable.class);
        String element2Text = "element2";

        // process SEL node
        when(webService.findElementsWithXpath(eq(pageVariable), anyString())).thenReturn(Arrays.asList(element1, element2));
        when(webService.getAttribute(element1, "innerHTML")).thenReturn(element1Text);
        when(webService.getAttribute(element2, "innerHTML")).thenReturn(element2Text);

        // run
        MetaExpression output = process(construct, page, xpath);

        // verify
        verify(webService, times(2)).getAttribute(any(), anyString());

        // assert
        Assert.assertEquals(output.getType(), LIST);
        @SuppressWarnings("unchecked")
        List<MetaExpression> result = (List<MetaExpression>) output.getValue();
        Assert.assertEquals(result.get(0).getStringValue(), element1Text);
        Assert.assertEquals(result.get(1).getStringValue(), element2Text);
    }

    /**
     * tests the process under normal circumstances with a page given.
     * We have a text query and findElements returns no items.
     */
    @Test
    public void testNormalUsageWithPageAndTextQueryAndNoElements() {
        // mock
        WebService webService = mock(WebService.class);
        XPathConstruct construct = new XPathConstruct();
        construct.setWebService(webService);

        // The page
        MetaExpression page = mockExpression(ATOMIC);
        PageVariable pageVariable = mock(PageVariable.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The xpath
        String xPathValue = "xpath";
        String shortenedXPathValue = "shorter xpath";
        MetaExpression xpath = mockExpression(ATOMIC);
        when(xpath.getStringValue()).thenReturn(xPathValue);

        // process SEL node
        when(webService.findElementsWithXpath(pageVariable, shortenedXPathValue)).thenReturn(Collections.emptyList());

        // run
        MetaExpression output = process(construct, page, xpath);

        // verify
        verify(webService, times(0)).getAttribute(any(), anyString());

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * tests the process under normal circumstances with a page given.
     * We have an attribute query and findElements returns more items.
     */
    @Test
    public void testNormalUsageWithPageAndAttributeQueryAndMoreElements() {
        // mock
        WebService webService = mock(WebService.class);
        XPathConstruct construct = new XPathConstruct();
        construct.setWebService(webService);

        // The page
        MetaExpression page = mockExpression(ATOMIC);
        PageVariable pageVariable = mock(PageVariable.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The xpath
        String xPathValue = "xpath/@attribute";
        MetaExpression xpath = mockExpression(ATOMIC);
        when(xpath.getStringValue()).thenReturn(xPathValue);

        // The WebElements
        WebVariable element1 = mock(WebVariable.class);
        String element1Text = "element1";
        String element1Result = "Result from getAttribute for element 1";
        WebVariable element2 = mock(WebVariable.class);
        String element2Text = "element2";
        String element2Result = "Result from getAttribute for element 2";

        // process SEL node

        when(webService.findElementsWithXpath(eq(pageVariable), anyString())).thenReturn(Arrays.asList(element1, element2));
        when(webService.getAttribute(element1, "innerHTML")).thenReturn(element1Text);
        when(webService.getAttribute(element2, "innerHTML")).thenReturn(element2Text);

        // The second attribute part
        when(webService.getAttribute(element1, "attribute")).thenReturn(element1Result);
        when(webService.getAttribute(element2, "attribute")).thenReturn(element2Result);

        // run
        MetaExpression output = process(construct, page, xpath);

        // verify
        verify(webService, times(2)).getAttribute(any(), anyString());

        // assert
        Assert.assertEquals(output.getType(), LIST);
        @SuppressWarnings("unchecked")
        List<MetaExpression> result = (List<MetaExpression>) output.getValue();
        Assert.assertEquals(result.get(0).getStringValue(), element1Result);
        Assert.assertEquals(result.get(1).getStringValue(), element2Result);
    }

    /**
     * tests the process under normal circumstances with a page given.
     * We have an attribute query but cannot find the attribute.
     */
    @Test
    public void testNormalUsageWithPageAndOneAttributeAndNoAttributeFound() {
        // mock
        WebService webService = mock(WebService.class);
        XPathConstruct construct = new XPathConstruct();
        construct.setWebService(webService);

        // The page
        MetaExpression page = mockExpression(ATOMIC);
        PageVariable pageVariable = mock(PageVariable.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The xpath
        String xPathValue = "xpath/@attribute";
        MetaExpression xpath = mockExpression(ATOMIC);
        when(xpath.getStringValue()).thenReturn(xPathValue);

        // The WebElements
        WebVariable element1 = mock(WebVariable.class);

        // process SEL node

        when(webService.findElementsWithXpath(eq(pageVariable), anyString())).thenReturn(Arrays.asList(element1));

        // The second attribute part
        when(webService.getAttribute(element1, "attribute")).thenReturn(null);

        // run
        MetaExpression output = process(construct, page, xpath);

        // verify
        verify(webService, times(1)).getAttribute(any(), anyString());

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
        XPathConstruct construct = new XPathConstruct();
        construct.setWebService(webService);
        MetaExpression page = mockExpression(ATOMIC);
        MetaExpression xpath = mockExpression(ATOMIC);
        when(page.isNull()).thenReturn(true);

        // run
        MetaExpression output = process(construct, page, xpath);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * tests the process under normal circumstances with a page given.
     * We have a text query and findElements only returns one item.
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "crash due to selenium")
    public void testProcessWhenCreateNodeFails() {
        // mock
        WebService webService = mock(WebService.class);
        XPathConstruct construct = new XPathConstruct();
        construct.setWebService(webService);

        // The page
        MetaExpression page = mockExpression(ATOMIC);
        PageVariable pageVariable = mock(PageVariable.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The xpath
        String xPathValue = "xpath";
        MetaExpression xpath = mockExpression(ATOMIC);
        when(xpath.getStringValue()).thenReturn(xPathValue);

        // The WebElement
        WebVariable element = mock(WebVariable.class);
        String elementText = "the text in the attribute of the element";

        // process SEL node
        when(webService.findElementsWithXpath(eq(pageVariable), anyString())).thenReturn(Collections.singletonList(element));
        when(webService.getAttribute(element, "outerHTML")).thenThrow(new RobotRuntimeException("crash due to selenium"));

        // run
        MetaExpression output = process(construct, page, xpath);
    }
}
