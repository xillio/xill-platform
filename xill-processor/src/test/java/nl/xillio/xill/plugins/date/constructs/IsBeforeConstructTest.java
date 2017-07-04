/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.plugins.date.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.date.data.Date;
import nl.xillio.xill.plugins.date.services.DateService;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class IsBeforeConstructTest extends TestUtils {
    /**
     * Test the process method under normal circumstances
     */
    @Test
    public void testProcess() {
        IsBeforeConstruct isBeforeConstruct = new IsBeforeConstruct();

        // Mock
        ZonedDateTime date1 = ZonedDateTime.now();
        ZonedDateTime date2 = ZonedDateTime.now();

        MetaExpression date1Expression = createDateTimeExpression(date1);
        MetaExpression date2Expression = createDateTimeExpression(date2);

        DateService dateService = mock(DateService.class);
        when(dateService.isBefore(any(), any())).thenReturn(false);
        isBeforeConstruct.setDateService(dateService);

        // Run
        MetaExpression result = process(isBeforeConstruct, date1Expression, date2Expression);

        // Verify
        verify(dateService).isBefore(any(), any());

        // Assert
        assertEquals(result.getBooleanValue(), false);
    }

    public static MetaExpression createDateTimeExpression(ZonedDateTime date1) {
        MetaExpression expression = fromValue("Date");
        expression.storeMeta(new Date(date1));
        return expression;
    }
}
