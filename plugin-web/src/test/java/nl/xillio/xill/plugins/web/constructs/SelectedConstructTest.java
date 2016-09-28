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
 * Test the {@link SelectedConstruct}.
 */
public class SelectedConstructTest extends TestUtils {

    /**
     * Test the process under normal circumstances.
     */
    @Test
    public void testProcessNormalUsage() {
        // mock
        WebService webService = mock(WebService.class);
        SelectedConstruct construct = new SelectedConstruct();
        construct.setWebService(webService);

        // The element
        NodeVariable nodeVariable = mock(NodeVariable.class);
        MetaExpression node = mockExpression(ATOMIC);
        when(node.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // the process
        when(webService.isSelected(nodeVariable)).thenReturn(true);

        // run
        MetaExpression output = process(construct, node);

        // verify
        verify(node, times(2)).getMeta(NodeVariable.class);
        verify(webService, times(1)).isSelected(nodeVariable);

        // assert
        Assert.assertEquals(output.getBooleanValue(), true);
    }

    /**
     * Test the process with null input given.
     */
    @Test
    public void testNullInput() {
        // mock
        WebService webService = mock(WebService.class);
        SelectedConstruct construct = new SelectedConstruct();
        construct.setWebService(webService);
        MetaExpression input = mockExpression(ATOMIC);
        when(input.isNull()).thenReturn(true);

        // run
        MetaExpression output = process(construct, input);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process when the webService fails.
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "I broke")
    public void testProcessWebServiceFailure() {
        // mock
        WebService webService = mock(WebService.class);
        SelectedConstruct construct = new SelectedConstruct();
        construct.setWebService(webService);

        // The element
        NodeVariable nodeVariable = mock(NodeVariable.class);
        MetaExpression node = mockExpression(ATOMIC);
        when(node.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // the process
        when(webService.isSelected(nodeVariable)).thenThrow(new RobotRuntimeException("I broke"));

        // run
        process(construct, node);
    }

    /**
     * Test the process when the webService fails.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessNoNodeGiven() {
        // mock
        WebService webService = mock(WebService.class);
        SelectedConstruct construct = new SelectedConstruct();
        construct.setWebService(webService);
        // The element
        MetaExpression node = mockExpression(ATOMIC);
        when(node.getMeta(NodeVariable.class)).thenReturn(null);

        // run
        process(construct, node);
    }
}
