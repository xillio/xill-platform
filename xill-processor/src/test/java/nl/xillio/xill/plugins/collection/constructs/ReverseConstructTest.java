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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.plugins.collection.services.reverse.Reverse;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Test the {@link ReverseConstruct}
 *
 * @author Sander Visser
 */
public class ReverseConstructTest extends ExpressionBuilderHelper {

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void testProcessWithNormalInput() {

        // mock
        String expectedOutput = "this is the expected Value";
        MetaExpression outputExpression = fromValue(expectedOutput);

        Reverse reverse = mock(Reverse.class);
        when(reverse.asReversed(extractValue(outputExpression), true)).thenReturn(true);

        MetaExpression recursive = mock(MetaExpression.class);
        when(recursive.getBooleanValue()).thenReturn(true);

        // run
        MetaExpression output = ReverseConstruct.process(outputExpression, recursive, reverse);

        // verify
        verify(reverse, times(1)).asReversed(extractValue(outputExpression), true);
        verify(recursive, times(1)).getBooleanValue();

        // assert
        Assert.assertEquals(output, TRUE);
    }
}
