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
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.date.services.DateService;
import nl.xillio.xill.plugins.date.services.DateServiceImpl;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.ZoneId;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

public class OfConstructTest extends TestUtils {

    private final OfConstruct ofConstruct = new OfConstruct();
    private final String zoneId = "Europe/Amsterdam";
    private final Date date = mock(Date.class);

    @BeforeClass
    public void initializeDateService() {
        ofConstruct.setDateService(new DateServiceImpl());
    }

    /**
     * Test the process method under normal circumstances
     */
    @Test
    public void testProcess() {
        // Mock
        DateService dateService = mockDateService(date);

        // Run
        MetaExpression dateExpression = process(ofConstruct, mockParameters(zoneId));

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
        mockDateService(date);
        MetaExpression[] values = mockParameters(zoneId);
        values[0] = NULL;

        // Run
        process(ofConstruct, values);
    }

    /**
     * Test the process method with an invalid zone
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "^.*Invalid\\szone\\sID.*$")
    public void testProcessInvalidZone() {
        // Mock
        mockDateService(date);

        // Run
        process(ofConstruct, mockParameters("Wrong"));
    }

    /**
     * Test the process method with an invalid range on one of the fields
     */
    @Test(expectedExceptions = OperationFailedException.class)
    public void testProcessInvalidInputRange() {
        process(ofConstruct, fromValue(1), fromValue(50), fromValue(1), fromValue(1), fromValue(1), fromValue(1));
    }

    /**
     * Mock a DateService with only a {@link DateService#constructDate(int, int, int, int, int, int, int, ZoneId)} method.
     *
     * @param date Date to return from the {@link DateService#constructDate(int, int, int, int, int, int, int, ZoneId)} method
     */
    private DateService mockDateService(Date date) {
        DateService dateService = mock(DateService.class);
        when(dateService.constructDate(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any())).thenReturn(date);
        ofConstruct.setDateService(dateService);
        return dateService;
    }

    /**
     * @param zoneId ZoneId to use in the parameters
     * @return An array of input parameters
     */
    private MetaExpression[] mockParameters(String zoneId) {
        return new MetaExpression[]{
                fromValue(0),
                fromValue(1),
                fromValue(2),
                fromValue(3),
                fromValue(4),
                fromValue(5),
                fromValue(6),
                fromValue(zoneId)
        };
    }
}
