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
package nl.xillio.xill.plugins.jdbc.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.plugins.jdbc.data.TemporalMetadataExpression;
import nl.xillio.xill.plugins.jdbc.services.TemporalConversionService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.temporal.Temporal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertSame;

public class TemporalConversionConstructTestBase extends TestUtils {
    protected TemporalConversionService temporalConversionService;
    protected Date dateSpecimen;
    protected Temporal temporalSpecimen;

    protected Construct construct;

    @BeforeMethod
    public void setUp() {
        temporalConversionService = mock(TemporalConversionService.class);
        dateSpecimen = mock(Date.class);
        temporalSpecimen = mock(Temporal.class);
    }

    @Test
    public void testConversion() {
        when(temporalConversionService.toDate(dateSpecimen)).thenReturn(temporalSpecimen);
        when(temporalConversionService.toTimestamp(dateSpecimen)).thenReturn(temporalSpecimen);

        MetaExpression result = process(construct, makeMeta(dateSpecimen));

        assertSame(result.getMeta(TemporalMetadataExpression.class).getData(), temporalSpecimen);
    }
}
