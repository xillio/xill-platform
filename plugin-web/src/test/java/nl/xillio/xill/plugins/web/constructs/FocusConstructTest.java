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
import nl.xillio.xill.plugins.web.data.NodeVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * The unit tests for the {@link FocusConstruct}.
 */
public class FocusConstructTest extends ExpressionBuilderHelper {

    /**
     * test the construct with normal input. No exceptions should be thrown, element.click is called once and output is NULL.
     */
    @Test
    public void testProcessNormalUsage() {
        // mock
        WebService webService = mock(WebService.class);

        // The input
        MetaExpression element = mock(MetaExpression.class);
        NodeVariable nodeVariable = mock(NodeVariable.class);
        when(element.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // run
        MetaExpression output = FocusConstruct.process(element, webService);

        // verify
        verify(element, times(2)).getMeta(NodeVariable.class);
        verify(webService, times(1)).moveToElement(nodeVariable);

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
        MetaExpression input = mock(MetaExpression.class);
        when(input.isNull()).thenReturn(true);

        // run
        MetaExpression output = FocusConstruct.process(input, webService);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * test the construct when no node is given.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessNoNodeGiven() {
        // mock
        WebService webService = mock(WebService.class);

        // The input
        MetaExpression element = mock(MetaExpression.class);
        when(element.getMeta(NodeVariable.class)).thenReturn(null);

        // run
        FocusConstruct.process(element, webService);

        // verify
        verify(element, times(1)).getMeta(NodeVariable.class);
    }

    /**
     * test the process when the webService fails.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessFailedToFocus() {
        // mock
        WebService webService = mock(WebService.class);

        // The input
        NodeVariable nodeVariable = mock(NodeVariable.class);
        MetaExpression element = mock(MetaExpression.class);
        when(element.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        doThrow(new RobotRuntimeException("Error")).when(webService).moveToElement(any());

        // run
        FocusConstruct.process(element, webService);

        // verify
        verify(element, times(1)).getMeta(NodeVariable.class);
    }
}
