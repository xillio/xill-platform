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
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.date.services.DateService;
import org.testng.annotations.Test;

import java.time.ZoneId;

import static nl.xillio.xill.plugins.date.utils.MockUtils.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

public class OfConstructTest {

    String zoneId = "Europe/Amsterdam";

    Date date = mock(Date.class);

    /**
     * Test the process method under normal circumstances
     */
    @Test
    public void testProcess() {
        // Mock
        DateService dateService = mockDateService(date);
        MetaExpression[] values = mockParameters(zoneId);

        // Run
        MetaExpression dateExpression = OfConstruct.process(values, dateService);

        // Verify
        verify(dateService).constructDate(0, 1, 2, 3, 4, 5, 6, ZoneId.of(zoneId));

        // Assert
        assertSame(dateExpression.getMeta(Date.class), date);
    }

    /**
     * Test the process method when one of the input values is null
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "^.*cannot\\sbe\\snull.*$")
    public void testProcessInputNull() {
        // Mock
        DateService dateService = mockDateService(date);
        MetaExpression[] values = mockParameters(zoneId);
        values[0] = mockNullExpression();

        // Run
        MetaExpression dateExpression = OfConstruct.process(values, dateService);

        // Verify
        verify(dateService, never()).constructDate(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());

        // Assert
    }

    /**
     * Test the process method with an invalid zone
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "^.*Invalid\\szone\\sID.*$")
    public void testProcessInvalidZone() {
        // Mock
        DateService dateService = mockDateService(date);
        MetaExpression[] values = mockParameters("Wrong");

        // Run
        MetaExpression dateExpression = OfConstruct.process(values, dateService);

        // Verify
        verify(dateService, never()).constructDate(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());

        // Assert
    }

    /**
     * Mock a DateService with only a {@link DateService#constructDate(int, int, int, int, int, int, int, ZoneId)} method.
     *
     * @param date Date to return from the {@link DateService#constructDate(int, int, int, int, int, int, int, ZoneId)} method
     */
    private DateService mockDateService(Date date) {
        DateService dateService = mock(DateService.class);
        when(dateService.constructDate(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any())).thenReturn(date);
        return dateService;
    }

    /**
     * @param zoneId ZoneId to use in the parameters
     * @return An array of input parameters for the {@link OfConstruct#process(MetaExpression[], DateService)} method
     */
    private MetaExpression[] mockParameters(String zoneId) {
        MetaExpression[] values =
                {mockIntExpression(0), mockIntExpression(1), mockIntExpression(2), mockIntExpression(3), mockIntExpression(4), mockIntExpression(5), mockIntExpression(6), mockStringExpression(zoneId)};
        return values;
    }
}
