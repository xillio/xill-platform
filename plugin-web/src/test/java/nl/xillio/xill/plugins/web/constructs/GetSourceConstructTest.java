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
import nl.xillio.xill.plugins.web.data.PageVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * test the {@link GetSourceConstruct}.
 */
public class GetSourceConstructTest extends TestUtils {

    /**
     * test the construct with normal input.
     */
    @Test
    public void testProcessNormalUsage() {

        String html = "HTML source";

        // mock
        WebService webService = mock(WebService.class);
        GetSourceConstruct construct = new GetSourceConstruct();
        construct.setWebService(webService);

        MetaExpression page = fromValue("MyPage");
        PageVariable pageVariable = mock(PageVariable.class);
        page.storeMeta(pageVariable);


        when(webService.getSource(pageVariable)).thenReturn(html);

        // run
        MetaExpression output = process(construct,page);

        // verify

        // the processItemMethod for the second variable
        verify(webService, times(1)).getSource(any());

        // assert
        Assert.assertEquals(output.getStringValue(), html);
    }

    /**
     * Test the process with null input given.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testNullInput() {
        // mock
        WebService webService = mock(WebService.class);
        GetSourceConstruct construct = new GetSourceConstruct();
        construct.setWebService(webService);

        // run
        MetaExpression output = process(construct,NULL);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * test the construct when no page is given.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessNoPageGiven() {
        // mock
        WebService webService = mock(WebService.class);
        GetSourceConstruct construct = new GetSourceConstruct();
        construct.setWebService(webService);
        // the input
        MetaExpression input = fromValue("No Page");

        // run
        process(construct,input);
    }
}
