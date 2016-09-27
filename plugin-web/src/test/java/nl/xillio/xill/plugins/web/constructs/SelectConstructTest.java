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
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Tests the {@link SelectConstruct}
 */
public class SelectConstructTest extends TestUtils {

    /**
     * Test the process under normal circumstances.
     */
    @Test
    public void testProcessNormalUsage() {
        // mock
        WebService webService = mock(WebService.class);
        SelectConstruct construct = new SelectConstruct();
        construct.setWebService(webService);

        // The element
        NodeVariable nodeVariable = mock(NodeVariable.class);
        MetaExpression node = mockExpression(ATOMIC);
        when(node.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // The select boolean
        MetaExpression select = mockExpression(ATOMIC);
        when(select.getBooleanValue()).thenReturn(true);

        // the process
        when(webService.isSelected(nodeVariable)).thenReturn(false);

        // run
        MetaExpression output = process(construct, node, select);

        // verify
        verify(node, times(2)).getMeta(NodeVariable.class);
        verify(webService, times(1)).isSelected(nodeVariable);
        verify(webService, times(1)).click(nodeVariable);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process with null page given.
     */
    @Test
    public void testNullInput() {
        // mock
        WebService webService = mock(WebService.class);
        SelectConstruct construct = new SelectConstruct();
        construct.setWebService(webService);
        MetaExpression page = mockExpression(ATOMIC);
        MetaExpression element = mockExpression(ATOMIC);
        when(page.isNull()).thenReturn(true);

        // run
        MetaExpression output = process(construct, page, element);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Tests the process when there is no need to take action.
     */
    @Test
    public void testProcessNoNeedToClick() {
        // mock
        WebService webService = mock(WebService.class);
        SelectConstruct construct = new SelectConstruct();
        construct.setWebService(webService);

        // The element
        NodeVariable nodeVariable = mock(NodeVariable.class);
        MetaExpression node = mockExpression(ATOMIC);
        when(node.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // The select boolean
        MetaExpression select = mockExpression(ATOMIC);
        when(select.getBooleanValue()).thenReturn(true);

        // the process
        when(webService.isSelected(nodeVariable)).thenReturn(true);

        // run
        MetaExpression output = process(construct, node, select);

        // verify
        verify(node, times(2)).getMeta(NodeVariable.class);
        verify(webService, times(1)).isSelected(nodeVariable);
        verify(webService, times(0)).click(nodeVariable);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Tests the process when no node is given.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessNoNodeGiven() {
        // mock
        WebService webService = mock(WebService.class);
        SelectConstruct construct = new SelectConstruct();
        construct.setWebService(webService);

        // The element
        MetaExpression node = mockExpression(ATOMIC);
        when(node.getMeta(NodeVariable.class)).thenReturn(null);

        // The select boolean
        MetaExpression select = mockExpression(ATOMIC);

        // run
        process(construct, node, select);
    }

    /**
     * Tests the process when the WebService breaks.
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "I broke.")
    public void testProcessWebServiceFailure() {
        // mock
        WebService webService = mock(WebService.class);
        SelectConstruct construct = new SelectConstruct();
        construct.setWebService(webService);

        // The element
        NodeVariable nodeVariable = mock(NodeVariable.class);
        MetaExpression node = mockExpression(ATOMIC);
        when(node.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // The select boolean
        MetaExpression select = mockExpression(ATOMIC);
        when(select.getBooleanValue()).thenReturn(false);

        // the process
        when(webService.isSelected(nodeVariable)).thenThrow(new RobotRuntimeException("I broke."));

        // run
        process(construct, node, select);
    }

}
