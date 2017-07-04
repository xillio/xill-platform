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
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.date.services.DateServiceImpl;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static nl.xillio.xill.plugins.date.constructs.IsBeforeConstructTest.createDateTimeExpression;
import static org.testng.Assert.assertEquals;

/**
 * Test the {@link FormatConstruct}
 *
 */
public class FormatConstructTest extends TestUtils {
    private final FormatConstruct construct = new FormatConstruct();

    @BeforeClass
    public void initializeConstruct() {
        construct.setDateService(new DateServiceImpl());
    }

    @Test
    public void testDateFormat() {
        assertEquals(
                process(
                        construct,
                        createDateTimeExpression(
                                ZonedDateTime.of(1532, 3, 4, 5, 3, 0, 0, ZoneId.of("Europe/Paris"))
                        ),
                        fromValue("yyyy-MM-dd")
                ),
                fromValue("1532-03-04")
        );
    }

    @Test
    public void testDateFormatWithLocale() {
        assertEquals(
                process(
                        construct,
                        createDateTimeExpression(
                                ZonedDateTime.of(1532, 3, 4, 5, 3, 0, 0, ZoneId.of("Europe/Paris"))
                        ),
                        fromValue("EEEE"),
                        fromValue("fr-FR")
                ),
                fromValue("vendredi")
        );
    }

    @Test
    public void testDefaultDateFormat() {

        assertEquals(
                process(
                        construct,
                        createDateTimeExpression(
                                ZonedDateTime.of(2020, 12, 12, 5, 5, 0, 0, ZoneId.of("UTC"))
                        )
                ),
                fromValue("2020-12-12 05:05:00")
        );
    }

    @Test(expectedExceptions = OperationFailedException.class)
    public void testInvalidPattern() {
        process(
                construct,
                createDateTimeExpression(
                        ZonedDateTime.now()
                ),
                fromValue("What Am I Doing?")
        );
    }
}
