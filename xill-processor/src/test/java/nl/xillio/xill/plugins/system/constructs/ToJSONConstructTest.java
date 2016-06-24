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
package nl.xillio.xill.plugins.system.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.services.json.JacksonParser;
import nl.xillio.xill.services.json.JsonException;
import nl.xillio.xill.services.json.JsonParser;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

/**
 * Test the {@link ToJSONConstruct}
 */
public class ToJSONConstructTest extends TestUtils {

    /**
     * Test the process method under normal circumstances
     */
    @Test
    public void testProcess() throws JsonException {
        // Mock context
        String output = "This is the output";
        MetaExpression input = mockExpression(ATOMIC);
        JsonParser parser = mock(JsonParser.class);
        when(parser.toJson(input)).thenReturn(output);

        MetaExpression pretty = mockExpression(ATOMIC);
        when(pretty.getBooleanValue()).thenReturn(false);

        // Run
        MetaExpression result = ToJSONConstruct.process(input, pretty, parser, null);

        // Verify
        verify(parser).toJson(input);

        // Assert
        Assert.assertSame(result.getStringValue(), output);
    }

    /**
     * Test the process method under normal circumstances with pretty printing
     */
    @Test
    public void testProcessPretty() throws JsonException {
        // Mock context
        String output = "This is the output";
        MetaExpression input = mockExpression(ATOMIC);
        JsonParser parser = mock(JsonParser.class);
        when(parser.toJson(input)).thenReturn(output);

        MetaExpression pretty = mockExpression(ATOMIC);
        when(pretty.getBooleanValue()).thenReturn(true);

        // Run
        MetaExpression result = ToJSONConstruct.process(input, pretty, null, parser);

        // Verify
        verify(parser).toJson(input);

        // Assert
        Assert.assertSame(result.getStringValue(), output);
    }

    /**
     * Test the process when a StackOverflowError occurs
     */
    @Test(expectedExceptions = {RobotRuntimeException.class})
    public void testProcessCircularReference() throws Exception {
        MetaExpression list = fromValue(new ArrayList<>());
        list.<ArrayList>getValue().add(list);

        JsonParser parser = new JacksonParser(false);

        // Run
        ToJSONConstruct.process(list, fromValue(false), parser, null);
    }
}
