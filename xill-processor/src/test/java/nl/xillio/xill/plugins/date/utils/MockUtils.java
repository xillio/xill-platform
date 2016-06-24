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
package nl.xillio.xill.plugins.date.utils;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.data.Date;

import java.time.ZonedDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility code for mocking different kinds of objects.
 *
 * @author Geert Konijnendijk
 */
public class MockUtils {
    /**
     * @param value Boolean value
     * @return A mocked {@link MetaExpression}
     */
    public static MetaExpression mockBoolExpression(boolean value) {
        MetaExpression expression = mock(MetaExpression.class);
        when(expression.getBooleanValue()).thenReturn(value);
        return expression;
    }

    /**
     * @param value {@link ZonedDateTime} value
     * @return A mocked {@link MetaExpression}
     */
    public static MetaExpression mockDateExpression(ZonedDateTime value) {
        MetaExpression expression = mock(MetaExpression.class);
        Date date = mock(Date.class);
        when(date.getZoned()).thenReturn(value);
        when(expression.getMeta(Date.class)).thenReturn(date);
        return expression;
    }

    public static MetaExpression mockDateExpression(Date date) {
        MetaExpression expression = mock(MetaExpression.class);
        when(expression.getMeta(Date.class)).thenReturn(date);
        return expression;
    }

    /**
     * @param value String value
     * @return A mocked {@link MetaExpression}
     */
    public static MetaExpression mockStringExpression(String value) {
        if (value == null)
            return mockNullExpression();
        MetaExpression expression = mock(MetaExpression.class);
        when(expression.getStringValue()).thenReturn(value);
        return expression;
    }

    /**
     * @param value Int value
     * @return A mocked {@link MetaExpression}
     */
    public static MetaExpression mockIntExpression(int value) {
        MetaExpression expression = mock(MetaExpression.class);
        when(expression.getNumberValue()).thenReturn(value);
        return expression;
    }

    /**
     * Mock a {@link MetaExpression} which will return true when isNull is called
     *
     * @return A mocked {@link MetaExpression}
     */
    public static MetaExpression mockNullExpression() {
        MetaExpression expression = mock(MetaExpression.class);
        when(expression.isNull()).thenReturn(true);
        return expression;
    }
}
