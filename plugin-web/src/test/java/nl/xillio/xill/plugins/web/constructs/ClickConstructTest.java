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
import nl.xillio.xill.plugins.web.data.WebVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.Assert;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

/**
 * test the {@link ClickConstruct}.
 */
public class ClickConstructTest extends TestUtils {


    /**
     * test the construct with normal input. No exceptions should be thrown, element.click is called once and output is NULL.
     */
    @Test
    public void testProcessNormalUsage() {
        // mock
        WebService webService = mock(WebService.class);
        ClickConstruct construct = new ClickConstruct();
        construct.setWebService(webService);

        MetaExpression input = mockExpression(ATOMIC);
        NodeVariable nodeVariable = mock(NodeVariable.class);
        when(input.getMeta(NodeVariable.class)).thenReturn(nodeVariable);

        // run
        MetaExpression output = process(construct,input);

        // verify
        verify(input, times(2)).getMeta(NodeVariable.class);
        verify(webService, times(1)).click(any());


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
        ClickConstruct construct = new ClickConstruct();
        construct.setWebService(webService);
        MetaExpression input = mockExpression(ATOMIC);

        when(input.getMeta(NodeVariable.class)).thenReturn(null);

        // run
        process(construct,input);
    }

    /**
     * Test the process with null input given.
     */
    @Test
    public void testNullInput() {
        //mock
        WebService webService = mock(WebService.class);
        ClickConstruct construct = new ClickConstruct();
        construct.setWebService(webService);

        // run
        MetaExpression output = process(construct,NULL);

        // assert
        Assert.assertEquals(output, NULL);
    }
}
