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

import static org.mockito.Mockito.*;

/**
 * Test the {@link SwitchFrameConstruct}.
 */
public class SwitchFrameConstructTest extends TestUtils {

    /**
     * Test the process under normal circumstances with a webelement given.
     */
    @Test
    public void testProcessWithWebElement() {
        // mock
        WebService webService = mock(WebService.class);
        SwitchFrameConstruct construct = new SwitchFrameConstruct();
        construct.setWebService(webService);

        // the page
        MetaExpression page = mockExpression(ATOMIC);
        PageVariable pageVariable = mock(PageVariable.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The element
        NodeVariable nodeVariable = mock(NodeVariable.class);
        MetaExpression node = mockExpression(ATOMIC);
        when(node.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // run
        MetaExpression output = process(construct,page, node);

        // verify
        verify(page, times(2)).getMeta(PageVariable.class);
        verify(node, times(2)).getMeta(NodeVariable.class);
        verify(webService, times(1)).switchToFrame(pageVariable, nodeVariable);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process under normal circumstances with an integer given.
     */
    @Test
    public void testProcessWithInteger() {
        // mock
        WebService webService = mock(WebService.class);
        SwitchFrameConstruct construct = new SwitchFrameConstruct();
        construct.setWebService(webService);

        // the page
        MetaExpression page = mockExpression(ATOMIC);
        PageVariable pageVariable = mock(PageVariable.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The frame
        int frameValue = 42;
        MetaExpression frame = fromValue(frameValue);

        // run
        MetaExpression output = process(construct,page, frame);

        // verify
        verify(webService, times(1)).switchToFrame(pageVariable, frameValue);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process under normal circumstances with a String given.
     */
    @Test
    public void testProcessWithString() {
        // mock
        WebService webService = mock(WebService.class);
        SwitchFrameConstruct construct = new SwitchFrameConstruct();
        construct.setWebService(webService);

        // the page
        MetaExpression page = mockExpression(ATOMIC);
        PageVariable pageVariable = mock(PageVariable.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The frame
        String frameValue = "frame as a String";
        MetaExpression frame = fromValue(frameValue);

        // run
        MetaExpression output = process(construct,page, frame);

        // verify
        verify(webService, times(1)).switchToFrame(pageVariable, frameValue);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process under normal circumstances with an invalid variable type given.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessInvalidVariableType() {
        // mock
        WebService webService = mock(WebService.class);
        SwitchFrameConstruct construct = new SwitchFrameConstruct();
        construct.setWebService(webService);

        // the page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mockExpression(ATOMIC);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The frame
        int frameValue = 42;
        MetaExpression frame = mockExpression(ATOMIC);
        when(frame.getValue()).thenReturn(null);
        when(frame.isNull()).thenReturn(true);
        when(frame.getMeta(NodeVariable.class)).thenReturn(null);
        when(frame.getType()).thenReturn(ATOMIC);
        when(frame.getNumberValue()).thenReturn(frameValue);

        // run
        process(construct,page, frame);
    }


    /**
     * Test the process with an invalid page handed.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testNoPageGiven() {
        // mock
        WebService webService = mock(WebService.class);
        SwitchFrameConstruct construct = new SwitchFrameConstruct();
        construct.setWebService(webService);

        // the page
        MetaExpression page = mockExpression(ATOMIC);
        MetaExpression frame = mockExpression(ATOMIC);

        // run
        process(construct,page, frame);
    }
}
