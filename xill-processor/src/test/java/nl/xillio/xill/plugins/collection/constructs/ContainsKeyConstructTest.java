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
package nl.xillio.xill.plugins.collection.constructs;

import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Test the {@link ContainsKeyConstruct}
 */
public class ContainsKeyConstructTest extends ExpressionBuilderHelper {

    /**
     * Test the process when the object does contain the key.
     */
    @Test
    public void testProcessObjectDoesContainKey() {
        testNormal(true);
    }

    /**
     * Test the process when the object does not contain the key.
     */
    @Test
    public void testProcessObjectDoesNotContainKey() {
        testNormal(false);
    }

    /**
     * Test the contains key construct under normal circumstances.
     *
     * @param expected Whether the map should contain the key, which should also be the result of the process method.
     */
    private void testNormal(boolean expected) {
        // The key.
        String keyString = "foo";
        MetaExpression key = mock(MetaExpression.class);
        when(key.getStringValue()).thenReturn(keyString);

        // The map.
        Map<String, MetaExpression> map = mock(Map.class);
        when(map.containsKey(keyString)).thenReturn(expected);
        MetaExpression object = mock(MetaExpression.class);
        when(object.getValue()).thenReturn(map);

        // Run.
        MetaExpression output = ContainsKeyConstruct.process(object, key);

        // Verify.
        verify(map, times(1)).containsKey(keyString);

        // Assert.
        Assert.assertEquals(output.getBooleanValue(), expected);
    }
}
