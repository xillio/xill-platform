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
package nl.xillio.xill.plugins.date.constructs;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.date.services.DateService;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;

import static nl.xillio.xill.plugins.date.utils.MockUtils.mockDateExpression;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class IsAfterConstructTest {
    /**
     * Test the process method under normal circumstances
     */
    @Test
    public void testProcess() {
        // Mock
        ZonedDateTime date1 = ZonedDateTime.now();
        ZonedDateTime date2 = ZonedDateTime.now();

        MetaExpression date1Expression = mockDateExpression(date1);
        MetaExpression date2Expression = mockDateExpression(date2);

        DateService dateService = mock(DateService.class);
        when(dateService.isAfter(any(), any())).thenReturn(false);

        // Run
        MetaExpression result = IsAfterConstruct.process(date1Expression, date2Expression, dateService);

        // Verify
        verify(dateService).isAfter(any(), any());

        // Assert
        assertEquals(result.getBooleanValue(), false);
    }
 }
