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

import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.math.BigInteger;

/**
 * This utility class provides some basic arithmetic operations on abstract Numbers.
 */
public class MathUtils {
    private static final Logger LOGGER = Log.get();
    private static int INT_MAX_VALUE_LENGTH = Integer.toString(Integer.MAX_VALUE).length();
    private static int LONG_MAX_VALUE_LENGTH = Long.toString(Long.MAX_VALUE).length();

    /**
     * Performs the arithmetic addition operation on two abstract numbers.
     *
     * @param a the left operand
     * @param b the right operand
     * @return the result of the operation
     * @throws UnsupportedOperationException if no supported number type is found
     */
    public static Number add(Number a, Number b) {
        NumberType type = NumberType.forClass(a.getClass(), b.getClass());
        switch (type) {
            case INT:
                Integer intR = addExactWithoutException(a.intValue(), b.intValue());
                if (intR != null) {
                    return intR;
                }
            case LONG:
                Long longR = addExactWithoutException(a.longValue(), b.longValue());
                if (longR != null) {
                    return longR;
                }
            case BIG:
                BigInteger bigA = getBig(a);
                BigInteger bigB = getBig(b);
                return bigA.add(bigB);
            case DOUBLE:
                return a.doubleValue() + b.doubleValue();
            default:
                throw new UnsupportedOperationException("Type " + type + " is not supported by the add operation.");
        }
    }

    private static BigInteger getBig(Number number) {
        if (number instanceof BigInteger) {
            return (BigInteger) number;
        }

        return BigInteger.valueOf(number.longValue());
    }

    /**
     * Performs the arithmetic subtraction operation on two abstract numbers.
     *
     * @param a the left operand
     * @param b the right operand
     * @return the result of the operation
     * @throws UnsupportedOperationException if no supported number type is found
     */
    public static Number subtract(Number a, Number b) {
        NumberType type = NumberType.forClass(a.getClass(), b.getClass());
        switch (type) {
            case INT:
                Integer intR = subtractExactWithoutException(a.intValue(), b.intValue());
                if (intR != null) {
                    return intR;
                }
            case LONG:
                Long longR = subtractExactWithoutException(a.longValue(), b.longValue());
                if (longR != null) {
                    return longR;
                }
            case BIG:
                BigInteger bigA = getBig(a);
                BigInteger bigB = getBig(b);
                return bigA.subtract(bigB);
            case DOUBLE:
                return a.doubleValue() - b.doubleValue();
            default:
                throw new UnsupportedOperationException("Type " + type + " is not supported by the subtract operation.");
        }
    }

    /**
     * Performs the arithmetic division operation on two abstract numbers.
     *
     * @param a the left operand
     * @param b the right operand
     * @return the result of the operation
     * @throws UnsupportedOperationException if no supported number type is found
     */
    public static Number divide(Number a, Number b) {
        NumberType type = NumberType.forClass(a.getClass(), b.getClass());
        switch (type) {
            case INT:
                if (b.intValue() == 0) {
                    if (a.intValue() == 0) {
                        return Double.NaN;
                    }

                    return a.intValue() > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                }
                int intResult = a.intValue() / b.intValue();
                if (intResult * b.intValue() == a.intValue()) {
                    return intResult;
                }
            case LONG:
                long longResult = a.longValue() / b.longValue();
                if (longResult * b.longValue() == a.longValue()) {
                    return longResult;
                }
            case BIG:
                BigInteger bigA = getBig(a);
                BigInteger bigB = getBig(b);
                BigInteger result = bigA.divide(bigB);
                if (result.multiply(bigB).equals(bigA)) {
                    return result;
                }
            case DOUBLE:
                return a.doubleValue() / b.doubleValue();
            default:
                throw new UnsupportedOperationException("Type " + type + " is not supported by the divide operation.");
        }
    }

    /**
     * Performs the arithmetic multiplication operation on two abstract numbers.
     *
     * @param a the left operand
     * @param b the right operand
     * @return the result of the operation
     * @throws UnsupportedOperationException if no supported number type is found
     */
    public static Number multiply(Number a, Number b) {
        NumberType type = NumberType.forClass(a.getClass(), b.getClass());
        switch (type) {
            case INT:
                Integer intR = multiplyExactWithoutException(a.intValue(), b.intValue());
                if (intR != null) {
                    return intR;
                }
            case LONG:
                Long longR = multiplyExactWithoutException(a.longValue(), b.longValue());
                if (longR != null) {
                    return longR;
                }
            case BIG:
                BigInteger bigA = getBig(a);
                BigInteger bigB = getBig(b);
                return bigA.multiply(bigB);
            case DOUBLE:
                return a.doubleValue() * b.doubleValue();
            default:
                throw new UnsupportedOperationException("Type " + type + " is not supported by the multiply operation.");
        }
    }

    /**
     * Performs the arithmetic modulo operation on two abstract numbers.
     *
     * @param a the left operand
     * @param b the right operand
     * @return the result of the operation
     * @throws UnsupportedOperationException if no supported number type is found
     */
    public static Number modulo(Number a, Number b) {
        NumberType type = NumberType.forClass(a.getClass(), b.getClass());
        switch (type) {
            case INT:
                if (b.intValue() == 0) {
                    return Double.NaN;
                }
                return a.intValue() % b.intValue();
            case LONG:
                if (b.longValue() == 0) {
                    return Double.NaN;
                }
                return a.longValue() % b.longValue();
            case BIG:
                BigInteger bigA = getBig(a);
                BigInteger bigB = getBig(b);
                if (bigB.equals(BigInteger.ZERO)) {
                    return Double.NaN;
                }
                return bigA.mod(bigB);
            case DOUBLE:
                return a.doubleValue() % b.doubleValue();
            default:
                throw new UnsupportedOperationException("Type " + type + " is not supported by the modulo operation.");
        }
    }

    /**
     * Performs the arithmetic exponential operation on two abstract numbers.
     *
     * @param a the left operand
     * @param b the right operand
     * @return the result of the operation
     * @throws UnsupportedOperationException if no supported number type is found
     */
    public static Number power(Number a, Number b) {
        NumberType type = NumberType.forClass(a.getClass(), b.getClass());
        switch (type) {
            case INT:
                return Math.pow(a.intValue(), b.intValue());
            case LONG:
                return Math.pow(a.longValue(), b.longValue());
            case BIG:
                BigInteger bigA = getBig(a);
                return bigA.pow(b.intValue());
            case DOUBLE:
                return Math.pow(a.doubleValue(), b.doubleValue());
            default:
                throw new UnsupportedOperationException("Type " + type + " is not supported by the power operation.");
        }
    }

    /**
     * Performs the comparison operation on two abstract numbers.
     *
     * @param a the left operand
     * @param b the right operand
     * @return the value {@code 0} if {@code x == y};
     * a value less than {@code 0} if {@code x < y}; and
     * a value greater than {@code 0} if {@code x > y}
     * @throws UnsupportedOperationException if no supported number type is found
     */
    public static int compare(Number a, Number b) {
        NumberType type = NumberType.forClass(a.getClass(), b.getClass());
        switch (type) {
            case INT:
                return Integer.compare(a.intValue(), b.intValue());
            case LONG:
                return Long.compare(a.longValue(), b.longValue());
            case BIG:
                BigInteger bigA = getBig(a);
                BigInteger bigB = getBig(b);
                return bigA.compareTo(bigB);
            case DOUBLE:
                return Double.compare(a.doubleValue(), b.doubleValue());
            default:
                throw new UnsupportedOperationException("Type " + type + " is not supported by the compare operation.");
        }
    }

    /**
     * Recreates the addExact method from Math.java to check for overflow while avoiding throwing and handling
     * Exceptions (including empty catch blocks).
     *
     * @return null if an overflow would occur, the result of the addition otherwise
     */
    static Integer addExactWithoutException(int x, int y) {
        int r = x + y;
        if (((x ^ r) & (y ^ r)) < 0) {
            return null;
        }
        return r;
    }

    /**
     * Recreates the addExact method from Math.java to check for overflow while avoiding throwing and handling
     * Exceptions (including empty catch blocks).
     *
     * @return null if an overflow would occur, the result of the addition otherwise
     */
    static Long addExactWithoutException(long x, long y) {
        long r = x + y;
        if (((x ^ r) & (y ^ r)) < 0) {
            return null;
        }
        return r;
    }

    /**
     * Recreates the subtractExact method from Math.java to check for overflow while avoiding throwing and handling
     * Exceptions (including empty catch blocks).
     *
     * @return null if an overflow would occur, the result of the subtraction otherwise
     */
    static Integer subtractExactWithoutException(int x, int y) {
        int r = x - y;
        if (((x ^ y) & (x ^ r)) < 0) {
            return null;
        }
        return r;
    }

    /**
     * Recreates the subtractExact method from Math.java to check for overflow while avoiding throwing and handling
     * Exceptions (including empty catch blocks).
     *
     * @return null if an overflow would occur, the result of the subtraction otherwise
     */
    static Long subtractExactWithoutException(long x, long y) {
        long r = x - y;
        if (((x ^ y) & (x ^ r)) < 0) {
            return null;
        }
        return r;
    }

    /**
     * Recreate the multiplyExact method from Math.java to check for overflow while avoiding throwing and handling
     * Exceptions (including empty catch blocks).
     *
     * @return null if an overflow would occur, the result of the multiplication otherwise
     */
    static Integer multiplyExactWithoutException(int x, int y) {
        long r = (long) x * (long) y;
        if ((int) r != r) {
            return null;
        }
        return (int) r;
    }

    /**
     * Recreate the multiplyExact method from Math.java to check for overflow while avoiding throwing and handling
     * Exceptions (including empty catch blocks).
     *
     * @return null if an overflow would occur, the result of the multiplication otherwise
     */
    static Long multiplyExactWithoutException(long x, long y) {
        long r = x * y;
        long ax = Math.abs(x);
        long ay = Math.abs(y);
        if (((ax | ay) >>> 31 != 0)) {
            // Some bits greater than 2^31 that might cause overflow
            // Check the result using the divide operator
            // and check for the special case of Long.MIN_VALUE * -1
            if (((y != 0) && (r / y != x)) ||
                    (x == Long.MIN_VALUE && y == -1)) {
                return null;
            }
        }
        return r;
    }

    /**
     * Tries to parse a number from a String. Returns {@link Double#NaN} if the argument could not be parsed.
     * @param value the String representing a number
     * @return the actual number or NaN
     */
    public static Number parse(String value) {
        try {
            return tryParse(value);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private static Number tryParse(String value) {
        if (value.contains(".")) {
            // This is a decimal value
            return Double.parseDouble(value);
        } else {
            return parseNonDecimal(value);
        }
    }

    private static Number parseNonDecimal(String value) {

        if (value.length() <= INT_MAX_VALUE_LENGTH) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignore) { // NO SONAR
                // Fallback
            }
        }

        if (value.length() <= LONG_MAX_VALUE_LENGTH) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignore) { // NO SONAR
                // Fallback
            }
        }

        try {
            return new BigInteger(value);
        } catch (NumberFormatException e) {
            LOGGER.error("Failed to parse " + value + " as a number", e);
            return Double.NaN;
        }
    }

    /**
     * The supported number types.
     * Note that these types should be ordered from low to high precedence.
     */
    private enum NumberType {
        DOUBLE(Double.class, Float.class),
        LONG(Long.class),
        BIG(BigInteger.class),
        INT(Integer.class, Byte.class, Short.class);

        private static final NumberType[] values = NumberType.values();
        private final Class<? extends Number>[] numberClasses;

        @SafeVarargs
        NumberType(Class<? extends Number>... numberClasses) {
            this.numberClasses = numberClasses;
        }

        /**
         * Tests whether this type supports a class.
         *
         * @param clazz the class
         * @return true if and only if this type supports the number
         */
        public boolean supports(Class<? extends Number> clazz) {
            for (Class<?> testClass : numberClasses) {
                if (testClass.isAssignableFrom(clazz)) {
                    return true;
                }
            }

            return false;
        }

        public static NumberType forClass(Class<? extends Number> classA, Class<? extends Number> classB) {
            for (NumberType type : values) {
                if (type.supports(classA) || type.supports(classB)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Incompatible number type for " + classA.getSimpleName() + " or " + classB);
        }
    }
}
