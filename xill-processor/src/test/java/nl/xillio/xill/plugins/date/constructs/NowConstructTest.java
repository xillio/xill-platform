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
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.plugins.date.services.DateService;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

/**
 * Tests the {@link NowConstruct}
 *
 * @author Geert Konijnendijk
 */
public class NowConstructTest {

    /**
     * Test the process method under the default circumstances
     */
    @Test
    public void testProcess() {
        // Mock

        // ZonedDateTime is final, don't mock
        Date mockDate = mock(Date.class);
        DateService dateService = mock(DateService.class);
        when(dateService.now()).thenReturn(mockDate);

        // Run
        MetaExpression date = NowConstruct.process(dateService);

        // Verify
        verify(dateService).now();

        // Assert
        assertSame(date.getMeta(Date.class), mockDate);
    }
}
