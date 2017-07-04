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
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.plugins.date.services.DateServiceImpl;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static nl.xillio.xill.plugins.date.constructs.IsBeforeConstructTest.createDateTimeExpression;
import static org.testng.Assert.assertEquals;

/**
 * Test the {@link LocalizedFormatConstruct}
 */
public class LocalizedFormatConstructTest extends TestUtils {
    private final LocalizedFormatConstruct construct = new LocalizedFormatConstruct();

    @BeforeClass
    public void initializeDateService() {
        construct.setDateService(new DateServiceImpl());
    }

    @Test
    public void testGetShortDate() {
        assertEquals(
                process(
                        construct,
                        createDateTimeExpression(ZonedDateTime.of(1432, 5, 5, 5, 0, 0, 0, ZoneId.of("UTC+3"))),
                        fromValue("fr-FR"),
                        fromValue("short"),
                        fromValue("short")
                ).getStringValue(),
                "05/05/32 05:00"
        );
    }

    @Test
    public void testDefaultsAreMedium() {
        ZonedDateTime date = ZonedDateTime.of(2644, 5, 5, 5, 3, 8, 0, ZoneId.of("UTC+6"));

        String defaultResult = process(
                construct,
                createDateTimeExpression(date),
                fromValue("en-US")
        ).getStringValue();

        String specifiedResult = process(
                construct,
                createDateTimeExpression(date),
                fromValue("en-US"),
                fromValue("medium"),
                fromValue("medium")
        ).getStringValue();

        assertEquals(defaultResult, "May 5, 2644 5:03:08 AM");
        assertEquals(defaultResult, specifiedResult);
    }

    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = ".*dateStyle.*")
    public void testInvalidDateStyle() {
        process(
                construct,
                createDateTimeExpression(ZonedDateTime.now()),
                fromValue("fr-FR"),
                fromValue("very long")
        );
    }

    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = ".*timeStyle.*")
    public void testInvalidTimeStyle() {
        process(
                construct,
                createDateTimeExpression(ZonedDateTime.now()),
                fromValue("fr-FR"),
                fromValue("long"),
                fromValue("nope")
        );
    }



}
