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
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.data.NodeVariable;
import nl.xillio.xill.plugins.web.data.PageVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.InvalidSelectorException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link CSSPathConstruct}.
 */
public class CSSPathConstructTest extends ExpressionBuilderHelper {

    /**
     * test the construct with normal input.
     */
    @Test
    public void testNormalNodeUsage() {
        // mock
        WebService webService = mock(WebService.class);

        // The element
        MetaExpression element = mock(MetaExpression.class);
        NodeVariable nodeVariable = mock(NodeVariable.class);
        when(element.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // The css path
        String query = "cssPath";
        MetaExpression cssPath = mock(MetaExpression.class);
        when(cssPath.getStringValue()).thenReturn(query);

        // The result from findElements
        NodeVariable firstResult = mock(NodeVariable.class);
        NodeVariable secondResult = mock(NodeVariable.class);

        // The process
        when(webService.findElementsWithCssPath(nodeVariable, query)).thenReturn(Arrays.asList(firstResult, secondResult));
        when(webService.getAttribute(eq(firstResult), anyString())).thenReturn("first");
        when(webService.getAttribute(eq(secondResult), anyString())).thenReturn("second");
        when(webService.createNodeVariable(nodeVariable, firstResult)).thenReturn(firstResult);
        when(webService.createNodeVariable(nodeVariable, secondResult)).thenReturn(secondResult);

        // run
        MetaExpression output = CSSPathConstruct.process(element, cssPath, webService);
        // assert
        Assert.assertEquals(output.getType(), LIST);
        @SuppressWarnings("unchecked")
        List<MetaExpression> result = (List<MetaExpression>) output.getValue();
        Assert.assertEquals(result.size(), 2);
    }

    /**
     * Test the process with null input given.
     */
    @Test
    public void testNullInput() {
        // mock
        WebService webService = mock(WebService.class);
        MetaExpression input = mock(MetaExpression.class);
        MetaExpression csspath = mock(MetaExpression.class);
        when(input.isNull()).thenReturn(true);

        // run
        MetaExpression output = CSSPathConstruct.process(input, csspath, webService);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * test the construct when a single resultvalue is returned.
     */
    @Test
    public void testProcessSingleResultValue() {
        // mock
        WebService webService = mock(WebService.class);

        // The element
        MetaExpression element = mock(MetaExpression.class);
        NodeVariable nodeVariable = mock(NodeVariable.class);
        when(element.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // The css path
        String query = "cssPath";
        MetaExpression cssPath = mock(MetaExpression.class);
        when(cssPath.getStringValue()).thenReturn(query);

        // The result of findElements
        NodeVariable firstResult = mock(NodeVariable.class);

        // The process
        when(webService.findElementsWithCssPath(nodeVariable, query)).thenReturn(Arrays.asList(firstResult));
        when(webService.getAttribute(eq(firstResult), anyString())).thenReturn("first");
        when(webService.createNodeVariable(nodeVariable, firstResult)).thenReturn(firstResult);

        // run
        MetaExpression output = CSSPathConstruct.process(element, cssPath, webService);

        // assert
        Assert.assertEquals(output.getType(), ATOMIC);
    }

    /**
     * test the process when no result can be found.
     */
    @Test
    public void testProcessNoValueFound() {
        // mock
        WebService webService = mock(WebService.class);

        // The element
        MetaExpression element = mock(MetaExpression.class);
        NodeVariable nodeVariable = mock(NodeVariable.class);
        when(element.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // The css path
        String query = "cssPath";
        MetaExpression cssPath = mock(MetaExpression.class);
        when(cssPath.getStringValue()).thenReturn(query);

        // The process
        when(webService.findElementsWithCssPath(nodeVariable, query)).thenReturn(Arrays.asList());

        // run
        MetaExpression output = CSSPathConstruct.process(element, cssPath, webService);

        // verify tht we stop before asking the attribute
        verify(webService, times(0)).getAttribute(any(), anyString());

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the construct when no node or page is given.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testNoNodeOrPageGiven() {
        // mock
        WebService webService = mock(WebService.class);

        // The element
        MetaExpression element = mock(MetaExpression.class);
        NodeVariable nodeVariable = mock(NodeVariable.class);

        // The css path
        String query = "cssPath";
        MetaExpression cssPath = mock(MetaExpression.class);
        when(cssPath.getStringValue()).thenReturn(query);

        // The process
        when(webService.findElementsWithCssPath(nodeVariable, query)).thenReturn(Arrays.asList());

        // run
        CSSPathConstruct.process(element, cssPath, webService);
    }


    /**
     * Test the construct when a page variable is given
     */
    @Test
    public void processPage(){
        // Mock
        WebService webService = mock(WebService.class);

        // The element
        MetaExpression element = mock(MetaExpression.class);
        PageVariable pageVariable = mock(PageVariable.class);

        when(element.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The css path
        String query = "cssPath";
        MetaExpression cssPath = mock(MetaExpression.class);
        when(cssPath.getStringValue()).thenReturn(query);

        // The process
        when(webService.findElementsWithCssPath(pageVariable,query)).thenReturn(Arrays.asList());

        // Run
        Assert.assertEquals(CSSPathConstruct.process(element,cssPath,webService), NULL);

    }

}
