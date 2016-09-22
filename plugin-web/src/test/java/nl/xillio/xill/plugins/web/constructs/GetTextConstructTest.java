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
import nl.xillio.xill.plugins.web.data.PageVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * test the {@link GetTextConstruct}.
 */
public class GetTextConstructTest extends TestUtils {

    /**
     * test the construct with normal input.
     * The input is a list with two values. One containing a NODE and with an input tag.
     * The other a PAGE with a textarea tag.
     */
    @Test
    public void testProcessNormalUsage() {
        // mock
        WebService webService = mock(WebService.class);
        GetTextConstruct construct = new GetTextConstruct();
        construct.setWebService(webService);

        // the first element in the list and what it uses
        MetaExpression first = mockExpression(ATOMIC);
        NodeVariable nodeVariable = mock(NodeVariable.class);
        when(first.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // the second element in the list and what it uses
        MetaExpression second = mockExpression(ATOMIC);
        PageVariable pageVariable = mock(PageVariable.class);
        when(second.getMeta(NodeVariable.class)).thenReturn(null);
        when(second.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // the process method
        MetaExpression elementList = mockExpression(LIST);
        when(elementList.isNull()).thenReturn(false);
        when(elementList.getType()).thenReturn(LIST);
        when(elementList.getValue()).thenReturn(Arrays.asList(first, second));

        // the processItem method for the first variable
        when(webService.getTagName(nodeVariable)).thenReturn("input");
        when(webService.getAttribute(eq(nodeVariable), anyString())).thenReturn("pet");

        // the processItem method for the second variable
        when(webService.getText(pageVariable)).thenReturn("master");

        // run
        MetaExpression output = process(construct,elementList);

        // verify

        // the processItemMethod for the first variable
        verify(webService, times(1)).getTagName(nodeVariable);
        verify(webService, times(1)).getAttribute(eq(nodeVariable), anyString());

        // the processItemMethod for the second variable
        verify(webService, times(0)).getTagName(pageVariable);
        verify(webService, times(0)).getAttribute(eq(pageVariable), anyString());
        verify(webService, times(1)).getText(any());

        // assert
        Assert.assertEquals(output.getStringValue(), "petmaster");
    }

    /**
     * Test the process with null input given.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testNullInput() {
        // mock
        WebService webService = mock(WebService.class);
        GetTextConstruct construct = new GetTextConstruct();
        construct.setWebService(webService);
        MetaExpression input = mockExpression(ATOMIC);
        when(input.isNull()).thenReturn(true);

        // run
        MetaExpression output = process(construct,input);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * test the construct with normal input.
     * The input is a ATOMIC object with no required tag.
     */
    @Test
    public void testProcessNoListWithNoTag() {
        // mock
        WebService webService = mock(WebService.class);
        GetTextConstruct construct = new GetTextConstruct();
        construct.setWebService(webService);

        // the input
        MetaExpression first = mockExpression(ATOMIC);
        NodeVariable nodeVariable = mock(NodeVariable.class);
        when(first.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // the process method
        when(first.isNull()).thenReturn(false);
        when(first.getType()).thenReturn(ATOMIC);

        // the processItem method for the variable
        when(webService.getTagName(nodeVariable)).thenReturn("No Input or TextArea");
        when(webService.getText(eq(nodeVariable))).thenReturn("pet");

        // run
        MetaExpression output = process(construct,first);

        // verify

        // the process method
        verify(first, times(0)).getValue();

        // the processItemMethod for the first variable
        verify(webService, times(0)).getAttribute(any(), anyString());

        // assert
        Assert.assertEquals(output.getStringValue(), "pet");
    }

    /**
     * test the construct when no node is given.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessNoNodeGiven() {
        // mock
        WebService webService = mock(WebService.class);
        GetTextConstruct construct = new GetTextConstruct();
        construct.setWebService(webService);
        // the input
        MetaExpression input = mockExpression(ATOMIC);
        when(input.getMeta(NodeVariable.class)).thenReturn(null);

        // run
        process(construct,input);
    }
}
