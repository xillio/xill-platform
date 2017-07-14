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
import nl.xillio.xill.plugins.date.data.Date;
import nl.xillio.xill.plugins.date.services.DateServiceImpl;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static nl.xillio.xill.plugins.date.constructs.IsBeforeConstructTest.createDateTimeExpression;
import static org.testng.Assert.assertEquals;

/**
 * Test the {@link ChangeConstruct}
 *
 * @author Geert Konijnendijk
 */
public class ChangeConstructTest extends TestUtils {
    private ChangeConstruct construct = new ChangeConstruct();

    @BeforeClass
    public void initializeConstruct() {
        construct.setDateService(new DateServiceImpl());
    }

    @Test
    public void testAYoungDevelopersLifetime() {
        assertEquals(
                process(
                        construct,
                        createDateTimeExpression(
                                ZonedDateTime.of(1994, 2, 5, 2, 0, 0, 0, ZoneId.of("Europe/Copenhagen"))
                        ),
                        createMap(
                                "months", 4,
                                "years", 23
                        )
                ).getMeta(Date.class).getZoned(),
                ZonedDateTime.of(2017, 6, 5, 2, 0, 0, 0, ZoneId.of("Europe/Copenhagen"))
        );
    }

    @Test
    public void testChangeTimezone() {
        assertEquals(
                process(
                        construct,
                        createDateTimeExpression(
                                ZonedDateTime.of(2017, 2, 5, 2, 0, 0, 0, ZoneId.of("Europe/Copenhagen"))
                        ),
                        createMap(
                                "zone", "Europe/Amsterdam"
                        )
                ).getMeta(Date.class).getZoned(),
                ZonedDateTime.of(2017, 2, 5, 2, 0, 0, 0, ZoneId.of("Europe/Amsterdam"))
        );
    }

    @Test
    public void testTimeZoneAndZoneAreEqual() {
        ZonedDateTime input = ZonedDateTime.of(2017, 2, 5, 2, 0, 0, 0, ZoneId.of("Europe/London"));

        ZonedDateTime timeZoneChange = process(
                construct,
                createDateTimeExpression(
                        input
                ),
                createMap(
                        "timezone", "Europe/Amsterdam"
                )
        ).getMeta(Date.class).getZoned();

        ZonedDateTime zoneChange = process(
                construct,
                createDateTimeExpression(
                        input
                ),
                createMap(
                        "zone", "Europe/Amsterdam"
                )
        ).getMeta(Date.class).getZoned();

        assertEquals(zoneChange, timeZoneChange);
    }

    @Test
    public void testIllegalUnitDoesNotErrorOrChange() {
        ZonedDateTime date = ZonedDateTime.now();

        MetaExpression expression = process(
                construct,
                createDateTimeExpression(date),
                createMap(
                        "unit-test", null
                )
        );

        assertEquals(expression.getMeta(Date.class).getZoned(), date);
    }
}
