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
import nl.xillio.xill.plugins.web.data.NodeVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Test the {@link InputConstruct}.
 */
public class InputConstructTest extends TestUtils {

    /**
     * Tests the construct under normal circumstances
     *
     * @throws Exception
     */
    @Test
    public void testProcessNormalUsage() throws Exception {
        // mock
        WebService webService = mock(WebService.class);
        InputConstruct construct = new InputConstruct();
        construct.setWebService(webService);
        // The input
        MetaExpression input = mockExpression(ATOMIC);
        NodeVariable nodeVariable = mock(NodeVariable.class);
        when(input.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // The text input
        MetaExpression text = mockExpression(ATOMIC);
        when(text.getStringValue()).thenReturn("Text");

        // run
        MetaExpression output = process(construct,input, text);

        // verify
        verify(input, times(2)).getMeta(NodeVariable.class);
        verify(webService, times(1)).clear(nodeVariable);
        verify(webService, times(1)).sendKeys(nodeVariable, "Text");

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
        InputConstruct construct = new InputConstruct();
        construct.setWebService(webService);
        MetaExpression input = mockExpression(ATOMIC);
        MetaExpression text = mockExpression(ATOMIC);
        when(input.isNull()).thenReturn(true);

        // run
        MetaExpression output = process(construct,input, text);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Tests the construct under normal circumstances
     *
     * @throws Exception
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessFailureToSendKeys() throws Exception {
        // mock
        WebService webService = mock(WebService.class);
        InputConstruct construct = new InputConstruct();
        construct.setWebService(webService);

        // The input
        MetaExpression input = mockExpression(ATOMIC);
        NodeVariable nodeVariable = mock(NodeVariable.class);
        when(input.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // The text input
        MetaExpression text = mockExpression(ATOMIC);
        when(text.getStringValue()).thenReturn("Text");
        doThrow(new RobotRuntimeException("I broke!")).when(webService).sendKeys(nodeVariable, "Text");

        // run
        process(construct,input, text);
    }

    /**
     * Test the process when no node was in the expression.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessNoNodeGiven() {
        // mock
        WebService webService = mock(WebService.class);
        InputConstruct construct = new InputConstruct();
        construct.setWebService(webService);

        // The input
        MetaExpression input = mockExpression(ATOMIC);

        // The text input
        MetaExpression text = mockExpression(ATOMIC);

        when(input.getMeta(NodeVariable.class)).thenReturn(null);

        // run
        MetaExpression output = process(construct,input, text);

        // verify
        verify(input, times(2)).getMeta(NodeVariable.class);

        // assert
        Assert.assertEquals(output, NULL);
    }

}
