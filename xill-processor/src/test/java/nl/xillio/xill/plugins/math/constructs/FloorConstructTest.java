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
package nl.xillio.xill.plugins.math.constructs;

import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.math.services.math.MathOperations;
import nl.xillio.xill.plugins.math.services.math.MathOperationsImpl;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class FloorConstructTest extends ExpressionBuilderHelper {
    private MathOperations math = new MathOperationsImpl();

    // The given and expected values.
    private Number[][] tests = new Number[][]{
            {1, 1},
            {1.2, 1},
            {-3.14, -4},
            {2.8, 2}
    };

    /**
     * Test the process.
     */
    @Test
    public void testProcess() {
        for (Number[] num : tests) {
            MetaExpression result = FloorConstruct.process(fromValue(num[0]), math);
            assertEquals(result.getNumberValue().doubleValue(), num[1].doubleValue());
        }
    }
}
