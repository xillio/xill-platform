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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.date.services.DateService;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

/**
 * Test the {@link FromTimestampConstruct}.
 */
public class FromTimestampConstructTest extends TestUtils {
    @Test
    public void testProcess() {
        long timestamp = 123456;
        FromTimestampConstruct fromTimestampConstruct = new FromTimestampConstruct();

        // Mock.
        DateService dateService = mock(DateService.class);
        Date parsed = mock(Date.class);
        when(dateService.fromTimestamp(timestamp)).thenReturn(parsed);
        fromTimestampConstruct.setDateService(dateService);

        // Process.
        MetaExpression result = process(fromTimestampConstruct, fromValue(timestamp));

        // Verify.
        verify(dateService).fromTimestamp(timestamp);

        // Assert.
        assertSame(result.getMeta(Date.class), parsed);
    }

    @Test(expectedExceptions = OperationFailedException.class)
    public void testProcessNan() {
        FromTimestampConstruct fromTimestampConstruct = new FromTimestampConstruct();

        // Process.
        process(fromTimestampConstruct, fromValue("not a number"));
    }
}
