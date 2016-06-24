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
package nl.xillio.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author titusn
 */

public class MathUtilsTest {

    @DataProvider(name = "integers")
    Object[][] edgeCaseIntegers() {
        return new Object[][]{
                {Integer.MAX_VALUE, 1},
                {Integer.MAX_VALUE - 1, 1},
                {Integer.MIN_VALUE, -1},
                {Integer.MIN_VALUE + 1, -1},
                {Integer.MAX_VALUE, 2},
                {Integer.MAX_VALUE - 2, 2},
                {Integer.MIN_VALUE, -2},
                {Integer.MIN_VALUE + 2, -2},
                {Integer.MIN_VALUE, Integer.MIN_VALUE},
                {Integer.MAX_VALUE, Integer.MAX_VALUE},
                {Integer.MIN_VALUE, Integer.MAX_VALUE},
                {Integer.MIN_VALUE, 0},
                {Integer.MAX_VALUE, 0},
                {0, 0},
                {0, 1},
                {0, -1}
        };
    }

    @DataProvider(name = "longs")
    Object[][] edgeCaseLongs() {
        return new Object[][]{
                {Long.MAX_VALUE, 1},
                {Long.MAX_VALUE - 1, 1},
                {Long.MIN_VALUE, -1},
                {Long.MIN_VALUE + 1, -1},
                {Long.MAX_VALUE, 2},
                {Long.MAX_VALUE - 2, 2},
                {Long.MIN_VALUE, -2},
                {Long.MIN_VALUE + 2, -2},
                {Long.MIN_VALUE, Long.MIN_VALUE},
                {Long.MAX_VALUE, Long.MAX_VALUE},
                {Long.MIN_VALUE, Long.MAX_VALUE},
                {Long.MIN_VALUE, 0},
                {Long.MAX_VALUE, 0},
                {0, 0},
                {0, 1},
                {0, -1}
        };
    }

    @Test(dataProvider = "integers")
    public void testAddExactWithoutException(final int x, final int y) {
        Integer actual = MathUtils.addExactWithoutException(x, y);
        try {
            int expected = Math.addExact(x, y);
            Assert.assertEquals((int) actual, expected);
        } catch (ArithmeticException ignore) {
            Assert.assertNull(actual);
        }
    }

    @Test(dataProvider = "longs")
    public void testAddExactWithoutException(final long x, final long y) {
        Long actual = MathUtils.addExactWithoutException(x, y);
        try {
            long expected = Math.addExact(x, y);
            Assert.assertEquals((long) actual, expected);
        } catch (ArithmeticException ignore) {
            Assert.assertNull(actual);
        }
    }

    @Test(dataProvider = "integers")
    public void testSubtractExactWithoutException(final int x, final int y) {
        Integer actual = MathUtils.subtractExactWithoutException(x, y);
        try {
            int expected = Math.subtractExact(x, y);
            Assert.assertEquals((int) actual, expected);
        } catch (ArithmeticException ignore) {
            Assert.assertNull(actual);
        }
    }

    @Test(dataProvider = "longs")
    public void testSubtractExactWithoutException(final long x, final long y) {
        Long actual = MathUtils.subtractExactWithoutException(x, y);
        try {
            long expected = Math.subtractExact(x, y);
            Assert.assertEquals((long) actual, expected);
        } catch (ArithmeticException ignore) {
            Assert.assertNull(actual);
        }
    }

    @Test(dataProvider = "integers")
    public void testMultiplyExactWithoutException(final int x, final int y) {
        Integer actual = MathUtils.multiplyExactWithoutException(x, y);
        try {
            int expected = Math.multiplyExact(x, y);
            Assert.assertEquals((int) actual, expected);
        } catch (ArithmeticException ignore) {
            Assert.assertNull(actual);
        }
    }

    @Test(dataProvider = "longs")
    public void testMultiplyExactWithoutException(final long x, final long y) {
        Long actual = MathUtils.multiplyExactWithoutException(x, y);
        try {
            long expected = Math.multiplyExact(x, y);
            Assert.assertEquals((long) actual, expected);
        } catch (ArithmeticException ignore) {
            Assert.assertNull(actual);
        }
    }
}
