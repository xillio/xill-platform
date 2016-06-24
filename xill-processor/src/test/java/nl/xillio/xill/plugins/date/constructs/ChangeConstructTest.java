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

import me.biesaart.utils.Log;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.plugins.date.services.DateService;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

/**
 * Test the {@link ChangeConstruct}
 *
 * @author Geert Konijnendijk
 */
public class ChangeConstructTest {

    private static final Logger log = Log.get();

    private DateService dateService;
    private Date date;
    private MetaExpression dateExpression;
    private LinkedHashMap<String, MetaExpression> changes;
    private MetaExpression changesExpression;

    /**
     * Setup for all tests in this class
     */
    @BeforeMethod
    public void setup() {
        // Mock DateService, simply returns the date passed in
        dateService = mock(DateService.class);

        // Answer returning the first argument as a ZonedDateTime
        Answer<Date> returnAsZonedDateTime = invocation -> invocation.getArgumentAt(0, Date.class);

        when(dateService.changeTimeZone(any(), any())).then(returnAsZonedDateTime);
        when(dateService.add(any(), any())).then(returnAsZonedDateTime);

        date = mock(Date.class);
        dateExpression = mock(MetaExpression.class);
        when(dateExpression.getMeta(Date.class)).thenReturn(date);
        changes = new LinkedHashMap<>();
        changesExpression = mock(MetaExpression.class);
        when(changesExpression.getValue()).thenReturn(changes);
    }

    /**
     * Test the process method with just time unit changes
     */
    @Test
    public void testProcessUnits() {
        // Mock
        addUnitsChange();

        // Run
        MetaExpression newDate = runProcess();

        // Verify
        verify(dateService).add(any(), any());
        verify(dateService, times(0)).changeTimeZone(any(), any());

        // Assert
        assertEqualDate(newDate);
    }

    /**
     * Test the process method with just time zone change
     */
    @Test
    public void testProcessZone() {
        // Mock
        addZoneChange();

        // Run
        MetaExpression newDate = runProcess();

        // Verify
        verify(dateService).changeTimeZone(any(), any());

        // Assert
        assertEqualDate(newDate);
    }

    /**
     * Test both time unit changes and time zone change
     */
    @Test
    public void testProcessUnitsZone() {
        // Mock
        addUnitsChange();
        addZoneChange();

        // Run
        MetaExpression newDate = runProcess();

        // Verify
        verify(dateService).add(any(), any());
        verify(dateService).changeTimeZone(any(), any());

        // Assert
        assertEqualDate(newDate);
    }

    /**
     * Test the process with no changes at all
     */
    @Test
    public void testProcessEmpty() {
        // Mock

        // Run
        MetaExpression newDate = runProcess();

        // Verify
        verify(dateService, times(0)).changeTimeZone(any(), any());

        // Assert
        assertSame(newDate.getMeta(Date.class), date);
    }

    private MetaExpression mockNumberExpression(int number) {
        MetaExpression integer = mock(MetaExpression.class);
        when(integer.getNumberValue()).thenReturn(number);
        return integer;
    }

    private void addUnitsChange() {
        changes.put("nanos", mockNumberExpression(42));
        changes.put("hours", mockNumberExpression(42));
        changes.put("centuries", mockNumberExpression(42));
    }

    private void addZoneChange() {
        MetaExpression zone = mock(MetaExpression.class);
        when(zone.getStringValue()).thenReturn("Europe/Amsterdam");
        changes.put("zone", zone);
    }

    private MetaExpression runProcess() {
        return ChangeConstruct.process(log, dateExpression, changesExpression, dateService);
    }

    private void assertEqualDate(MetaExpression newDate) {
        assertEquals(newDate.getMeta(Date.class), date);
    }
}
