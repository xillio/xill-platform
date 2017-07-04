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
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.date.services.DateService;
import nl.xillio.xill.plugins.date.services.DateServiceImpl;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;

import static nl.xillio.xill.plugins.date.constructs.IsBeforeConstructTest.createDateTimeExpression;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * Test the {@link FormatConstruct}
 *
 * @author Geert Konijnendijk
 */
public class FormatConstructTest extends TestUtils {
    private FormatConstruct formatConstruct = new FormatConstruct();

    @BeforeClass
    public void initializeDateService() {
        formatConstruct.setDateService(new DateServiceImpl());
    }

    @DataProvider(name = "format")
    private Object[][] formatProvider() {
        ZonedDateTime date = ZonedDateTime.now();
        return new Object[][]{
                {createDateTimeExpression(date), fromValue("yyyy-MM-dd"), NULL},
                {createDateTimeExpression(date), fromValue("yyyy-MM-dd"), fromValue("en-US")},
                {createDateTimeExpression(date), NULL, NULL},
                {createDateTimeExpression(date), NULL, fromValue("en-US")}};
    }

    /**
     * Test the process method with both null and non-null format variable
     */
    @Test(dataProvider = "format")
    public void testProcess(MetaExpression dateExpression, MetaExpression formatExpression, MetaExpression localeExpression) {
        // Mock
        DateService dateService = mock(DateService.class);
        String returnString = "2015-8-3";
        when(dateService.formatDate(any(), any(), any())).thenReturn(returnString);

        FormatConstruct formatConstruct = new FormatConstruct();
        formatConstruct.setDateService(dateService);

        // Run
        MetaExpression formatted = process(formatConstruct, dateExpression, formatExpression, localeExpression);

        // Verify
        verify(dateService).formatDate(any(), any(), any());

        // Assert
        assertEquals(formatted.getStringValue(), returnString);
    }

    @Test(expectedExceptions = OperationFailedException.class)
    public void testIllegalFormat() {
        process(
                formatConstruct,
                createDateTimeExpression(ZonedDateTime.now()),
                fromValue("this is not a format")
        );
    }
}
