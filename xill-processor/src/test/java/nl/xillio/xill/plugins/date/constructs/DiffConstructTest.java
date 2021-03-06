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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static nl.xillio.xill.plugins.date.constructs.IsBeforeConstructTest.createDateTimeExpression;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * Tests for the {@link DiffConstruct}
 *
 * @author Geert Konijnendijk
 */
public class DiffConstructTest extends TestUtils {
    /**
     * @return Data containing two maps (which should be returned from the mock {@link DateService#difference(nl.xillio.xill.api.data.Date, nl.xillio.xill.api.data.Date, boolean)}) for testing absolute
     * and relative difference.
     */
    @DataProvider(name = "differences")
    private Object[][] mapProvider() {
        Map<String, Double> absoluteDifference = new HashMap<>();
        absoluteDifference.put("Unit1", 10.0);
        absoluteDifference.put("Unit2", 20.0);
        MetaExpression trueExpression = fromValue(true);

        Map<String, Double> relativeDifference = new HashMap<>();
        relativeDifference.put("Unit1", -10.0);
        relativeDifference.put("Unit2", 20.0);
        MetaExpression falseExpression = fromValue(false);

        return new Object[][]{{absoluteDifference, trueExpression}, {relativeDifference, falseExpression}};
    }

    /**
     * Test the process method
     *
     * @param differences Map with differences as could be returned by {@link DateService#difference(nl.xillio.xill.api.data.Date, nl.xillio.xill.api.data.Date, boolean)}.
     * @param absolute    Whether the difference should be absolute or relative
     */
    @Test(dataProvider = "differences")
    public void testProcess(Map<String, Number> differences, MetaExpression absolute) {
        DiffConstruct diffConstruct = new DiffConstruct();

        ZonedDateTime date1 = ZonedDateTime.now();
        ZonedDateTime date2 = ZonedDateTime.now();

        MetaExpression date1Expression = createDateTimeExpression(date1);
        MetaExpression date2Expression = createDateTimeExpression(date2);

        // Mock
        DateService dateService = mock(DateService.class);
        when(dateService.difference(any(), any(), anyBoolean())).thenReturn(differences);
        diffConstruct.setDateService(dateService);

        // Run
        MetaExpression difference = process(diffConstruct, date1Expression, date2Expression, absolute);

        // Verify
        verify(dateService).difference(any(), any(), anyBoolean());

        // Assert
        (difference.<Map<String, MetaExpression>>getValue()).forEach((k, v) -> assertEquals(differences.get(k).doubleValue(), v.getNumberValue().doubleValue(), 10e-9));
    }
}
