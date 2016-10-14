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

import java.math.BigInteger;

import static org.testng.Assert.assertEquals;

/**
 * @author titusn
 */

public class MathUtilsTest {

    /**
     * @return An array in the form {@code {{leftOperand, rightOperand, expectedResult}}}
     */
    @DataProvider(name = "addition")
    Object[][] additionNumbers() {
        return new Object[][] {
                {10, 5, 15},
                {10.0, 5.0, 15.0},
                {Integer.MAX_VALUE, 1, Integer.MAX_VALUE + 1L},
                {Long.MAX_VALUE, 1,  BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE)},
                {2147483648L, 10, 2147483658L},
                {new BigInteger("18446744073709551616"), BigInteger.TEN, new BigInteger("18446744073709551626")}
        };
    }

    @Test(dataProvider="addition")
    public void testAdd(Number a, Number b, Number expected) {
        assertEquals(MathUtils.add(a, b), expected);
    }

    /**
     * @return An array in the form {@code {{leftOperand, rightOperand, expectedResult}}}
     */
    @DataProvider(name = "subtraction")
    Object[][] subtractionNumbers() {
        return new Object[][] {
                {10, 5, 5},
                {10.0, 5.0, 5.0},
                {Integer.MIN_VALUE, 1, Integer.MIN_VALUE - 1L},
                {Long.MIN_VALUE, 1,  BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE)},
                {2147483648L, 10, 2147483638L},
                {new BigInteger("18446744073709551616"), BigInteger.TEN, new BigInteger("18446744073709551606")}
        };
    }

    @Test(dataProvider="subtraction")
    public void testSubtract(Number a, Number b, Number expected) {
        assertEquals(MathUtils.subtract(a, b), expected);
    }

    /**
     * @return An array in the form {@code {{leftOperand, rightOperand, expectedResult}}}
     */
    @DataProvider(name = "division")
    Object[][] divisionNumbers() {
        return new Object[][] {
                {10, 5, 2},
                {10.0, 5.0, 2.0},
                {5, 2, 2.5},
                {4294967296L, 2L, 2147483648L},
                {4294967296L, 10L, 429496729.6},
                {new BigInteger("18446744073709551616"), BigInteger.valueOf(2), new BigInteger("9223372036854775808")},
                {new BigInteger("18446744073709551616"), BigInteger.TEN, 1844674407370955160.6},
                {0, 0, Double.NaN},
                {1, 0, Double.POSITIVE_INFINITY},
                {-1, 0, Double.NEGATIVE_INFINITY},
                {0L, 0L, Double.NaN},
                {1L, 0L, Double.POSITIVE_INFINITY},
                {-1L, 0L, Double.NEGATIVE_INFINITY},
                {BigInteger.ZERO, BigInteger.ZERO, Double.NaN},
                {BigInteger.ONE, BigInteger.ZERO, Double.POSITIVE_INFINITY},
                {BigInteger.valueOf(-1), BigInteger.ZERO, Double.NEGATIVE_INFINITY}
        };
    }

    @Test(dataProvider="division")
    public void testDivision(Number a, Number b, Number expected) {
        assertEquals(MathUtils.divide(a, b), expected);
    }

    /**
     * @return An array in the form {@code {{leftOperand, rightOperand, expectedResult}}}
     */
    @DataProvider(name = "multiplication")
    Object[][] multiplicationNumbers() {
        return new Object[][] {
                {10, 5, 50},
                {10.0, 5.0, 50.0},
                {2147483648L, 2L, 4294967296L},
                {Integer.MAX_VALUE, 2, 4294967294L},
                {Long.MAX_VALUE, 2L, new BigInteger("18446744073709551614")},
                {new BigInteger("18446744073709551616"), BigInteger.valueOf(2), new BigInteger("36893488147419103232")},
        };
    }

    @Test(dataProvider="multiplication")
    public void testMultiplication(Number a, Number b, Number expected) {
        assertEquals(MathUtils.multiply(a, b), expected);
    }

    /**
     * @return An array in the form {@code {{leftOperand, rightOperand, expectedResult}}}
     */
    @DataProvider(name = "modulo")
    Object[][] moduloNumbers() {
        return new Object[][] {
                {10, 3, 1},
                {10.0, 3.0, 1.0},
                {2147483649L, 2L, 1L},
                {new BigInteger("18446744073709551617"), BigInteger.valueOf(2), BigInteger.ONE},
                {10, 0, Double.NaN},
                {10.0, 0.0, Double.NaN},
                {2147483649L, 0L, Double.NaN},
                {new BigInteger("18446744073709551617"), BigInteger.ZERO, Double.NaN},
        };
    }

    @Test(dataProvider="modulo")
    public void testModulo(Number a, Number b, Number expected) {
        assertEquals(MathUtils.modulo(a, b), expected);
    }

    /**
     * @return An array in the form {@code {{leftOperand, rightOperand, expectedResult}}}
     */
    @DataProvider(name = "power")
    Object[][] powerNumbers() {
        return new Object[][] {
                // Integer powers return a double
                {10, 3, 1000.0},
                {10.0, 3.0, 1000.0},
                // Long powers return a double
                {2147483649L, 2L, 4611686022722355201.0},
                {new BigInteger("2147483649"), BigInteger.valueOf(2), new BigInteger("4611686022722355201")},
        };
    }

    @Test(dataProvider="power")
    public void testPower(Number a, Number b, Number expected) {
        assertEquals(MathUtils.power(a, b), expected);
    }

    /**
     * @return An array in the form {@code {{leftOperand, rightOperand, expectedResult}}}
     */
    @DataProvider(name = "compare")
    Object[][] compareNumbers() {
        return new Object[][] {
                {10, 3, 1},
                {3, 10, -1},
                {3, 3, 0},
                {10.0, 3.0, 1},
                {3.0, 10.0, -1},
                {3.0, 3.0, 0},
                {10L, 3L, 1},
                {3L, 10L, -1},
                {3L, 3L, 0},
                {BigInteger.valueOf(10), BigInteger.valueOf(3), 1},
                {BigInteger.valueOf(3), BigInteger.valueOf(10), -1},
                {BigInteger.valueOf(3), BigInteger.valueOf(3), 0}
        };
    }

    @Test(dataProvider="compare")
    public void testCompare(Number a, Number b, Number expected) {
        assertEquals(MathUtils.compare(a, b), expected);
    }

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
            assertEquals((int) actual, expected);
        } catch (ArithmeticException ignore) {
            Assert.assertNull(actual);
        }
    }

    @Test(dataProvider = "longs")
    public void testAddExactWithoutException(final long x, final long y) {
        Long actual = MathUtils.addExactWithoutException(x, y);
        try {
            long expected = Math.addExact(x, y);
            assertEquals((long) actual, expected);
        } catch (ArithmeticException ignore) {
            Assert.assertNull(actual);
        }
    }

    @Test(dataProvider = "integers")
    public void testSubtractExactWithoutException(final int x, final int y) {
        Integer actual = MathUtils.subtractExactWithoutException(x, y);
        try {
            int expected = Math.subtractExact(x, y);
            assertEquals((int) actual, expected);
        } catch (ArithmeticException ignore) {
            Assert.assertNull(actual);
        }
    }

    @Test(dataProvider = "longs")
    public void testSubtractExactWithoutException(final long x, final long y) {
        Long actual = MathUtils.subtractExactWithoutException(x, y);
        try {
            long expected = Math.subtractExact(x, y);
            assertEquals((long) actual, expected);
        } catch (ArithmeticException ignore) {
            Assert.assertNull(actual);
        }
    }

    @Test(dataProvider = "integers")
    public void testMultiplyExactWithoutException(final int x, final int y) {
        Integer actual = MathUtils.multiplyExactWithoutException(x, y);
        try {
            int expected = Math.multiplyExact(x, y);
            assertEquals((int) actual, expected);
        } catch (ArithmeticException ignore) {
            Assert.assertNull(actual);
        }
    }

    @Test(dataProvider = "longs")
    public void testMultiplyExactWithoutException(final long x, final long y) {
        Long actual = MathUtils.multiplyExactWithoutException(x, y);
        try {
            long expected = Math.multiplyExact(x, y);
            assertEquals((long) actual, expected);
        } catch (ArithmeticException ignore) {
            Assert.assertNull(actual);
        }
    }
}
