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
package nl.xillio.xill.plugins.collection.data.range;

import nl.xillio.util.MathUtils;
import nl.xillio.xill.TestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.NoSuchElementException;

/**
 * The test class for {@link RangeIterator}.
 *
 * @author Pieter Soels
 */
public class RangeIteratorTest extends TestUtils {
    @Test
    public void testDefaultBehavior() throws Exception {
        // Initialize
        RangeIterator rangeIterator = new RangeIterator(0, 10, 1);
        int count = 0;

        // Run
        while (rangeIterator.hasNext()) {
            Assert.assertEquals(rangeIterator.next(), count++);
        }

        // Verify
        Assert.assertFalse(rangeIterator.hasNext());
        Assert.assertEquals(count, 10);
    }

    @Test
    public void testDefaultBehaviorDoubles() throws Exception {
        // Initialize
        RangeIterator rangeIterator = new RangeIterator(0.0, 10.0, 1.5);
        double count = -1.5;

        // Run
        while (rangeIterator.hasNext()) {
            count += 1.5;
            Assert.assertEquals(MathUtils.compare(rangeIterator.next(), count), 0);
        }

        // Verify
        Assert.assertFalse(rangeIterator.hasNext());
        Assert.assertEquals(MathUtils.compare(count, 9.0), 0);
    }

    @Test(expectedExceptions = NoSuchElementException.class)
    public void testExceptionUponNextWhenEmpty() throws Exception {
        // Initialize
        RangeIterator rangeIterator = new RangeIterator(0, 0, -1);

        // Run
        rangeIterator.next();
    }

    @Test
    public void testHasNextEmptyCollection() throws Exception {
        // Initialize
        RangeIterator rangeIterator = new RangeIterator(0.0, 0.0, 1.5);

        // Verify
        Assert.assertFalse(rangeIterator.hasNext());
    }

    @Test
    public void testHasNextDoesNotConsume() throws Exception {
        // Initialize
        RangeIterator rangeIterator = new RangeIterator(0, 1, 1);

        // Verify
        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(rangeIterator.hasNext());
        }
    }

    @Test
    public void testSingleItemIterator() throws Exception {
        // Initialize
        RangeIterator rangeIterator = new RangeIterator(0, -1, -1);

        // Verify
        Assert.assertTrue(rangeIterator.hasNext());

        // Run
        Assert.assertEquals(rangeIterator.next(), 0);

        // Verify
        Assert.assertFalse(rangeIterator.hasNext());
    }
}