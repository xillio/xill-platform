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
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.plugins.date.services.DateService;
import nl.xillio.xill.plugins.date.services.DateServiceImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

/**
 * Test the {@link ParseConstruct}
 *
 * @author Geert Konijnendijk
 */
public class ParseConstructTest extends TestUtils {

    private ParseConstruct parseConstruct = new ParseConstruct();

    @BeforeClass
    private void initializeConstruct() {
        parseConstruct.setDateService(new DateServiceImpl());
    }

    @DataProvider(name = "dateFormat")
    private Object[][] generateDateAndFormat() {
        MetaExpression formatString = fromValue("yyyy-MM-dd");
        MetaExpression dateString = fromValue("2015-08-03");
        MetaExpression localeString = fromValue("nl-NL");
        MetaExpression nullExpression = NULL;
        return new Object[][]{
                {dateString, formatString, nullExpression},
                {nullExpression, formatString, nullExpression},
                {dateString, nullExpression, nullExpression},
                {nullExpression, nullExpression, nullExpression},
                {dateString, formatString, localeString},
                {nullExpression, formatString, localeString},
                {dateString, nullExpression, localeString},
                {nullExpression, nullExpression, localeString}};
    }

    /**
     * Test the process method under normal circumstances
     *
     * @param dateString   Date variable
     * @param formatString Format variable
     */
    @Test(dataProvider = "dateFormat")
    public void testProcess(MetaExpression dateString, MetaExpression formatString, MetaExpression localeString) {
        // Mock
        DateService dateService = mock(DateService.class);
        // ZonedDateTime is final, don't mock
        Date parsed = mock(Date.class);
        when(dateService.now()).thenReturn(parsed);
        when(dateService.parseDate(any(), any(), any())).thenReturn(parsed);

        // Run
        MetaExpression parsedExpression = process(
                parseConstruct, dateString, formatString, localeString);

        // Verify
        if (dateString.isNull()) {
            verify(dateService).now();
            verify(dateService, never()).parseDate(any(), any(), any());
        } else {
            verify(dateService, never()).now();
            verify(dateService).parseDate(dateString.getStringValue(), formatString.getStringValue(), localeString.getStringValue());
        }

        // Assert
        assertSame(parsedExpression.getMeta(Date.class), parsed);
    }

    @Test
    public void testDefaultValueLocale() {
        String format = "EEE MMM dd yyyy HH:mm:ss 'GMT'Z";

        MetaExpression resultEnglish = process(
                parseConstruct,
                fromValue("Mon Jun 12 2017 09:52:45 GMT+0200"),
                fromValue(format),
                fromValue("en-US")
        );

        MetaExpression resultDefault = process(
                parseConstruct,
                fromValue("Mon Jun 12 2017 09:52:45 GMT+0200"),
                fromValue(format)
        );

        Assert.assertEquals(resultEnglish, resultDefault);
    }
}
