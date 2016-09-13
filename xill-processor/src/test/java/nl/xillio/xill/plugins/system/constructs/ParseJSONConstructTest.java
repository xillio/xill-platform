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
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.services.json.JsonException;
import nl.xillio.xill.services.json.JsonParser;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test the {@link ParseJSONConstruct}
 */
public class ParseJSONConstructTest extends TestUtils {

    /**
     * Test the process method under normal circumstances
     */
    @Test
    public void testProcess() throws JsonException {
        // Initialize
        String json = "[\"this\", \"is\", \"valid\", \"json\"]";
        ArrayList<String> expectedOutput = new ArrayList<>();
        expectedOutput.add("this");
        expectedOutput.add("is");
        expectedOutput.add("valid");
        expectedOutput.add("json");
        MetaExpression expression = fromValue(json);

        // Mock context
        JsonParser parser = mock(JsonParser.class);
        when(parser.fromJson(eq(json), any())).thenReturn(expectedOutput);

        // Run method
        MetaExpression result = ParseJSONConstruct.process(expression, parser);

        // Verify calls to service
        verify(parser).fromJson(eq(json), any());

        // Assertions
        Assert.assertEquals((ArrayList) result.getValue(), parseObject(expectedOutput).getValue());
        Assert.assertTrue(result.getValue() instanceof ArrayList);
    }

    /**
     * Test the process method under normal circumstances
     */
    @Test(expectedExceptions = InvalidUserInputException.class)
    public void testNestedListException() throws JsonException {
        // Initialize
        ArrayList<MetaExpression> list = new ArrayList<>();
        ArrayList<MetaExpression> nestedList = new ArrayList<>();
        nestedList.add(fromValue(true));
        list.add(fromValue("test"));
        list.add(fromValue(nestedList));

        // Mock context
        JsonParser parser = mock(JsonParser.class);

        // Run method
        ParseJSONConstruct.process(fromValue(list), parser);
    }

    /**
     * Test the process method with null input
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessNullInput() {
        // Run method
        ParseJSONConstruct.process(NULL, null);
    }

    /**
     * Test the process method with empty list as input
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessNullListInput() {
        ArrayList<MetaExpression> list = new ArrayList<>();
        list.add(NULL);

        // Run method
        ParseJSONConstruct.process(fromValue(list), null);
    }

    /**
     * Test the process method with empty list as input
     */
    @Test(expectedExceptions = InvalidUserInputException.class)
    public void testProcessEmptyListInput() {
        // Run method
        ParseJSONConstruct.process(fromValue(new ArrayList<>()), null);
    }

    /**
     * Test the process method with invalid json
     *
     * @throws Throwable while testing
     */
    @Test(expectedExceptions = OperationFailedException.class, expectedExceptionsMessageRegExp = "Could not parse JSON input..*")
    public void testProcessInvalid() throws Throwable {
        // Mock context
        MetaExpression expression = mockExpression(ATOMIC);
        JsonParser parser = mock(JsonParser.class);
        when(parser.fromJson(anyString(), any())).thenThrow(new JsonException("CORRECT", null));

        // Run method
        ParseJSONConstruct.process(expression, parser);
    }
}
