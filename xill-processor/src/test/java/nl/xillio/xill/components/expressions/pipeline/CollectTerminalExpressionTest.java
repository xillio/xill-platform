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
package nl.xillio.xill.components.expressions.pipeline;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.MetaExpression;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;


public class CollectTerminalExpressionTest extends TestUtils {
    private final Debugger debugger = mock(Debugger.class);

    @Test
    public void testRunNormal() {
        MetaExpression input = parseObject(asList(1, 2, 3, 4, 67, 2, 7, 2, 7, 7, 3, 1));
        input.registerReference();

        CollectTerminalExpression expression = new CollectTerminalExpression(input);

        MetaExpression result = expression.process(debugger).get();

        assertEquals(result, input);
    }

}