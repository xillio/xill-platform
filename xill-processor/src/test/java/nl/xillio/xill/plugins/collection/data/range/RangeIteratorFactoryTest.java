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

import nl.xillio.xill.TestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The test class for {@link RangeIteratorFactory}.
 *
 * @author Pieter Soels
 */
public class RangeIteratorFactoryTest extends TestUtils {
    @Test
    public void testNormalUsageWithoutStepAscending() throws Exception {
        // Initialize
        RangeIteratorFactory rangeIteratorFactory = new RangeIteratorFactory();

        // Run
        RangeIterator iterator = rangeIteratorFactory.createIterator(0, 10, null);

        // Verify
        ArrayList<Number> output = new ArrayList<>();
        Number[] expectedOutputArray = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ArrayList<Number> expectedOutput = new ArrayList<>(Arrays.asList(expectedOutputArray));

        Assert.assertTrue(iterator.hasNext());
        while (iterator.hasNext()) {
            output.add(iterator.next());
        }
        Assert.assertFalse(iterator.hasNext());

        Assert.assertEquals(output, expectedOutput);
    }

    @Test
    public void testNormalUsageWithoutStepDescending() throws Exception {
        // Initialize
        RangeIteratorFactory rangeIteratorFactory = new RangeIteratorFactory();

        // Run
        RangeIterator iterator = rangeIteratorFactory.createIterator(10, 0, null);

        // Verify
        ArrayList<Number> output = new ArrayList<>();
        Number[] expectedOutputArray = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        ArrayList<Number> expectedOutput = new ArrayList<>(Arrays.asList(expectedOutputArray));

        Assert.assertTrue(iterator.hasNext());
        while (iterator.hasNext()) {
            output.add(iterator.next());
        }
        Assert.assertFalse(iterator.hasNext());

        Assert.assertEquals(output, expectedOutput);
    }

    @Test
    public void testNormalUsageWithStepAscending() throws Exception {
        // Initialize
        RangeIteratorFactory rangeIteratorFactory = new RangeIteratorFactory();

        // Run
        RangeIterator iterator = rangeIteratorFactory.createIterator(0, 10, 2);

        // Verify
        ArrayList<Number> output = new ArrayList<>();
        Number[] expectedOutputArray = {0, 2, 4, 6, 8};
        ArrayList<Number> expectedOutput = new ArrayList<>(Arrays.asList(expectedOutputArray));

        Assert.assertTrue(iterator.hasNext());
        while (iterator.hasNext()) {
            output.add(iterator.next());
        }
        Assert.assertFalse(iterator.hasNext());

        Assert.assertEquals(output, expectedOutput);
    }

    @Test
    public void testNormalUsageWithStepDescending() throws Exception {
        // Initialize
        RangeIteratorFactory rangeIteratorFactory = new RangeIteratorFactory();

        // Run
        RangeIterator iterator = rangeIteratorFactory.createIterator(10, -10, -1.5);

        // Verify
        ArrayList<Number> output = new ArrayList<>();
        Number[] expectedOutputArray = {10, 8.5, 7.0, 5.5, 4.0, 2.5, 1.0, -0.5, -2.0, -3.5, -5.0, -6.5, -8.0, -9.5};
        ArrayList<Number> expectedOutput = new ArrayList<>(Arrays.asList(expectedOutputArray));

        Assert.assertTrue(iterator.hasNext());
        while (iterator.hasNext()) {
            output.add(iterator.next());
        }
        Assert.assertFalse(iterator.hasNext());

        Assert.assertEquals(output, expectedOutput);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testZeroStepException() throws Exception {
        // Initialize
        RangeIteratorFactory rangeIteratorFactory = new RangeIteratorFactory();

        // Run
        RangeIterator iterator = rangeIteratorFactory.createIterator(10, 0, 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDoubleMinValueStepException() throws Exception {
        // Initialize
        RangeIteratorFactory rangeIteratorFactory = new RangeIteratorFactory();

        // Run
        rangeIteratorFactory.createIterator(10, 0, Double.MIN_VALUE);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStartSameAsEndException() throws Exception {
        // Initialize
        RangeIteratorFactory rangeIteratorFactory = new RangeIteratorFactory();

        // Run
        rangeIteratorFactory.createIterator(0, 0, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testPositiveStepDecreasingRangeException() throws Exception {
        // Initialize
        RangeIteratorFactory rangeIteratorFactory = new RangeIteratorFactory();

        // Run
        rangeIteratorFactory.createIterator(0, 10, -1.5);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativeStepIncreasingRangeException() throws Exception {
        // Initialize
        RangeIteratorFactory rangeIteratorFactory = new RangeIteratorFactory();

        // Run
        rangeIteratorFactory.createIterator(0, -10.0, 1);
    }
}