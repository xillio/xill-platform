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

import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Test the {@link TypeOfConstruct}
 */
public class TypeOfConstructTest {

    /**
     * Test the only possible usage
     */
    @Test
    public void testProcess() {
        // Mock context
        for (ExpressionDataType expectedType : ExpressionDataType.values()) {
            MetaExpression expression = mock(MetaExpression.class);
            when(expression.getType()).thenReturn(expectedType);

            // Run
            MetaExpression result = TypeOfConstruct.process(expression);

            // Verify
            verify(expression).getType();

            // Assert
            Assert.assertSame(result.getStringValue(), expectedType.toString());
        }
    }
}
