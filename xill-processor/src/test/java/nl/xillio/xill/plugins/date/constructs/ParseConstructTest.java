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
import nl.xillio.xill.plugins.date.services.DateServiceImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Test the {@link ParseConstruct}
 *
 */
public class ParseConstructTest extends TestUtils {

    private ParseConstruct construct = new ParseConstruct();

    @BeforeClass
    private void initializeConstruct() {
        construct.setDateService(new DateServiceImpl());
    }

    @Test
    public void testParseDate() {
        assertEquals(
                process(
                        construct,
                        fromValue("maandag 10 juli"),
                        fromValue("EEEE dd MMMM"),
                        fromValue("nl-NL")
                ).getMeta(Date.class).getZoned(),
                ZonedDateTime.of(2017, 7, 10, 0, 0, 0, 0, ZoneId.systemDefault())
        );
    }

    @Test
    public void testTodayDefaultFormat() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime result = process(construct).getMeta(Date.class).getZoned();

        assertTrue(now.isBefore(result.plusSeconds(1)));
        assertTrue(now.isAfter(result.minusMinutes(1)));
    }

    @Test(expectedExceptions = OperationFailedException.class, expectedExceptionsMessageRegExp = ".*format.*")
    public void testInvalidPattern() {
        process(
                construct,
                fromValue("2016"),
                fromValue("I DONT KNOW!")
        );
    }
}
