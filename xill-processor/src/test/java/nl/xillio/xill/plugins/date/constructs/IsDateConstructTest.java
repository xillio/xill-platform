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
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.date.data.Date;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;

import static org.testng.Assert.assertEquals;

/**
 * Test class for the IsDate construct of the Date plugin
 *
 * @author Pieter Dirk Soels
 */
public class IsDateConstructTest extends TestUtils {
    private final IsDateConstruct construct = new IsDateConstruct();

    /**
     * Test the process method under normal circumstances with a date expression
     */
    @Test
    public void testProcess() {
        // Initialize
        MetaExpression dateExpression = fromValue("Placeholder value");
        dateExpression.storeMeta(new Date(ZonedDateTime.now()));

        // Run
        MetaExpression result = process(construct, dateExpression);

        // Assert
        assertEquals(result, TRUE);
    }

    /**
     * Test the process method under normal circumstances with a non-date expression
     */
    @Test
    public void testNotADate() {
        // Run
        MetaExpression result = process(construct, fromValue(true));

        // Assert
        assertEquals(result, FALSE);
    }

    /**
     * Test the process method with null as input
     */
    @Test
    public void testNullInputDate() {
        // Run
        MetaExpression result = process(construct, NULL);

        // Assert
        assertEquals(result, FALSE);
    }
}
