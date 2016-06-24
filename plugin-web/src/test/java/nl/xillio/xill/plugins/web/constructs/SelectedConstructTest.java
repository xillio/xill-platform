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

import static org.mockito.Mockito.*;

/**
 * Test the {@link SelectedConstruct}.
 */
public class SelectedConstructTest extends ExpressionBuilderHelper {

    /**
     * Test the process under normal circumstances.
     */
    @Test
    public void testProcessNormalUsage() {
        // mock
        WebService webService = mock(WebService.class);

        // The element
        NodeVariable nodeVariable = mock(NodeVariable.class);
        MetaExpression node = mock(MetaExpression.class);
        when(node.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // the process
        when(webService.isSelected(nodeVariable)).thenReturn(true);

        // run
        MetaExpression output = SelectedConstruct.process(node, webService);

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
        MetaExpression input = mock(MetaExpression.class);
        when(input.isNull()).thenReturn(true);

        // run
        MetaExpression output = SelectedConstruct.process(input, webService);

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

        // The element
        NodeVariable nodeVariable = mock(NodeVariable.class);
        MetaExpression node = mock(MetaExpression.class);
        when(node.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // the process
        when(webService.isSelected(nodeVariable)).thenThrow(new RobotRuntimeException("I broke"));

        // run
        SelectedConstruct.process(node, webService);
    }

    /**
     * Test the process when the webService fails.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessNoNodeGiven() {
        // mock
        WebService webService = mock(WebService.class);

        // The element
        MetaExpression node = mock(MetaExpression.class);
        when(node.getMeta(NodeVariable.class)).thenReturn(null);

        // run
        SelectedConstruct.process(node, webService);
    }
}
