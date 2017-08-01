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
import nl.xillio.xill.plugins.date.services.DateService;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static nl.xillio.xill.plugins.date.constructs.IsBeforeConstructTest.createDateTimeExpression;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * Test the {@link InfoConstruct}
 *
 * @author Geert Konijnendijk
 */
public class InfoConstructTest extends TestUtils {

    /**
     * Test the process method and see if it returns the correct keys in the returned map
     */
    @Test
    public void testProcess() {
        InfoConstruct infoConstruct = new InfoConstruct();

        // Mock
        DateService dateService = mock(DateService.class);
        Map<String, Long> fieldValues = new HashMap<>();
        fieldValues.put("Field1", 10L);
        fieldValues.put("Field2", 20L);
        fieldValues.put("Field3", 30L);
        when(dateService.getFieldValues(any())).thenReturn(fieldValues);
        ZoneId zoneId = mock(ZoneId.class);
        when(zoneId.toString()).thenReturn("Europe/Amsterdam");
        when(dateService.getTimezone(any())).thenReturn(zoneId);

        when(dateService.isInFuture(any())).thenReturn(true);
        when(dateService.isInPast(any())).thenReturn(false);
        // ZonedDateTime is final, don't mock
        ZonedDateTime date = ZonedDateTime.now();
        MetaExpression dateExpression = createDateTimeExpression(date);
        infoConstruct.setDateService(dateService);

        // Run
        MetaExpression info = process(infoConstruct, dateExpression);

        // Verify
        verify(dateService).getFieldValues(any());
        verify(dateService).getTimezone(any());
        verify(dateService).isInFuture(any());
        verify(dateService).isInPast(any());

        // Assert
        Map<String, MetaExpression> infoMap = info.getValue();
        fieldValues.forEach((k, v) -> assertEquals(infoMap.get(k).getNumberValue().longValue(), (long) v));
        assertEquals(infoMap.get("timeZone").getStringValue(), zoneId.toString());
        assertEquals(infoMap.get("isInFuture").getBooleanValue(), true);
        assertEquals(infoMap.get("isInPast").getBooleanValue(), false);
    }
}
