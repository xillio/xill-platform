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
package nl.xillio.xill.plugins.collection.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.MetaExpressionIterator;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.plugins.collection.data.range.RangeIterator;
import nl.xillio.xill.plugins.collection.data.range.RangeIteratorFactory;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Pieter Soels
 */
public class RangeConstructTest extends TestUtils {
    @Test
    public void testProcessWithoutStep() throws Exception {
        // Mock
        RangeIteratorFactory rangeIteratorFactory = mock(RangeIteratorFactory.class);
        RangeIterator rangeIterator = mock(RangeIterator.class);
        when(rangeIteratorFactory.createIterator(any(), any(), any())).thenReturn(rangeIterator);

        // Initialize
        RangeConstruct construct = new RangeConstruct(rangeIteratorFactory);

        // Run
        MetaExpression result = process(construct, fromValue(1), fromValue(10));

        // Assert
        assertTrue(result.hasMeta(MetaExpressionIterator.class));
        assertEquals(result.getStringValue(), "[Ranged iterator]");
    }

    @Test
    public void testProcessWithStep() throws Exception {
        // Mock
        RangeIteratorFactory rangeIteratorFactory = mock(RangeIteratorFactory.class);
        RangeIterator rangeIterator = mock(RangeIterator.class);
        when(rangeIteratorFactory.createIterator(any(), any(), any())).thenReturn(rangeIterator);

        // Initialize
        RangeConstruct construct = new RangeConstruct(rangeIteratorFactory);

        // Run
        MetaExpression result = process(construct, fromValue(1), fromValue(10), fromValue(1));

        // Assert
        assertTrue(result.hasMeta(MetaExpressionIterator.class));
        assertEquals(result.getStringValue(), "[Ranged iterator]");
    }

    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = ".*Test.*")
    public void testProcessExceptionHandling() throws Exception {
        // Mock
        RangeIteratorFactory rangeIteratorFactory = mock(RangeIteratorFactory.class);
        when(rangeIteratorFactory.createIterator(any(), any(), any())).thenThrow(new IllegalArgumentException("Test"));

        // Initialize
        RangeConstruct construct = new RangeConstruct(rangeIteratorFactory);

        // Run
        process(construct, fromValue(0), fromValue(0));
    }
}