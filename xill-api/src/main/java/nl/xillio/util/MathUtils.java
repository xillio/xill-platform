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
    private static final int INT_MAX_VALUE_LENGTH = Integer.toString(Integer.MAX_VALUE).length();
    private static final int LONG_MAX_VALUE_LENGTH = Long.toString(Long.MAX_VALUE).length();

    /**
     * Private constructor since this class contains only static methods
     */
    private MathUtils() {}

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
        return type.add(a, b);
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
        return type.subtract(a, b);
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
        return type.divide(a, b);
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
        return type.multiply(a, b);
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
        return type.modulo(a, b);
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
        return type.power(a, b);
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
        return type.compare(a, b);
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
        // Some bits greater than 2^31 that might cause overflow
        // Check the result using the divide operator
        // and check for the special case of Long.MIN_VALUE * -1
        if ((ax | ay) >>> 31 != 0 && testMultiplyOverflow(x, y, r)) {
                return null;
        }
        return r;
    }

    /**
     * Test if a multiplication has overflown
     * @param x left operand
     * @param y right operand
     * @param result x*y
     * @return true if the result of the multiplication is incorrect
     */
    private static boolean testMultiplyOverflow(long x, long y, long result) {
        return ((y != 0) && (result / y != x)) ||
                (x == Long.MIN_VALUE && y == -1);
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

    private static BigInteger getBig(Number number) {
        if (number instanceof BigInteger) {
            return (BigInteger) number;
        }

        return BigInteger.valueOf(number.longValue());
    }

    /**
     * The supported number types.
     * Note that these types should be ordered from low to high precedence.
     */
    private enum NumberType {
        DOUBLE(Double.class, Float.class) {
            @Override
            public Number add(Number a, Number b) {
                return a.doubleValue() + b.doubleValue();
            }

            @Override
            public Number subtract(Number a, Number b) {
                return a.doubleValue() - b.doubleValue();
            }

            @Override
            public Number divide(Number a, Number b) {
                return a.doubleValue() / b.doubleValue();
            }

            @Override
            public Number multiply(Number a, Number b) {
                return a.doubleValue() * b.doubleValue();
            }

            @Override
            public Number modulo(Number a, Number b) {
                return a.doubleValue() % b.doubleValue();
            }

            @Override
            public Number power(Number a, Number b) {
                return Math.pow(a.doubleValue(), b.doubleValue());
            }

            @Override
            public int compare(Number a, Number b) {
                return Double.compare(a.doubleValue(), b.doubleValue());
            }
        },
        LONG(Long.class){
            @Override
            public Number add(Number a, Number b) {
                Long longR = addExactWithoutException(a.longValue(), b.longValue());
                if (longR != null) {
                    return longR;
                } else {
                    return BIG.add(a, b);
                }
            }

            @Override
            public Number subtract(Number a, Number b) {
                Long longR = subtractExactWithoutException(a.longValue(), b.longValue());
                if (longR != null) {
                    return longR;
                } else {
                    return BIG.subtract(a, b);
                }
            }

            @Override
            public Number divide(Number a, Number b) {
                if (b.longValue() == 0) {
                    if (a.longValue() == 0) {
                        return Double.NaN;
                    }

                    return a.longValue() > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                }

                long longResult = a.longValue() / b.longValue();
                // Check if the result is a whole number
                if (longResult * b.longValue() == a.longValue()) {
                    return longResult;
                } else {
                    return DOUBLE.divide(a, b);
                }
            }

            @Override
            public Number multiply(Number a, Number b) {
                Long longR = multiplyExactWithoutException(a.longValue(), b.longValue());
                if (longR != null) {
                    return longR;
                } else {
                    return BIG.multiply(a, b);
                }
            }

            @Override
            public Number modulo(Number a, Number b) {
                if (b.longValue() == 0) {
                    return Double.NaN;
                }
                return a.longValue() % b.longValue();
            }

            @Override
            public Number power(Number a, Number b) {
                return Math.pow(a.longValue(), b.longValue());
            }

            @Override
            public int compare(Number a, Number b) {
                return Long.compare(a.longValue(), b.longValue());
            }
        },
        BIG(BigInteger.class){
            @Override
            public Number add(Number a, Number b) {
                BigInteger bigA = getBig(a);
                BigInteger bigB = getBig(b);
                return bigA.add(bigB);
            }

            @Override
            public Number subtract(Number a, Number b) {
                BigInteger bigA = getBig(a);
                BigInteger bigB = getBig(b);
                return bigA.subtract(bigB);
            }

            @Override
            public Number divide(Number a, Number b) {
                BigInteger bigA = getBig(a);
                BigInteger bigB = getBig(b);

                if (bigB.equals(BigInteger.ZERO)) {
                    int sign = bigA.signum();
                    if (sign == 0) {
                        return Double.NaN;
                    } else if (sign == 1) {
                        return Double.POSITIVE_INFINITY;
                    } else {
                        return Double.NEGATIVE_INFINITY;
                    }
                }

                BigInteger result = bigA.divide(bigB);
                // Check if the result is a whole number
                if (result.multiply(bigB).equals(bigA)) {
                    return result;
                } else {
                    return DOUBLE.divide(a, b);
                }
            }

            @Override
            public Number multiply(Number a, Number b) {
                BigInteger bigA = getBig(a);
                BigInteger bigB = getBig(b);
                return bigA.multiply(bigB);
            }

            @Override
            public Number modulo(Number a, Number b) {
                BigInteger bigA = getBig(a);
                BigInteger bigB = getBig(b);
                if (bigB.equals(BigInteger.ZERO)) {
                    return Double.NaN;
                }
                return bigA.mod(bigB);
            }

            @Override
            public Number power(Number a, Number b) {
                BigInteger bigA = getBig(a);
                return bigA.pow(b.intValue());
            }

            @Override
            public int compare(Number a, Number b) {
                BigInteger bigA = getBig(a);
                BigInteger bigB = getBig(b);
                return bigA.compareTo(bigB);
            }
        },
        INT(Integer.class, Byte.class, Short.class){
            @Override
            public Number add(Number a, Number b) {
                Integer intR = addExactWithoutException(a.intValue(), b.intValue());
                // Try to add as longs when overflow occurs
                if (intR != null) {
                    return intR;
                } else {
                    return LONG.add(a, b);
                }
            }

            @Override
            public Number subtract(Number a, Number b) {
                Integer intR = subtractExactWithoutException(a.intValue(), b.intValue());
                if (intR != null) {
                    return intR;
                } else {
                    return LONG.subtract(a, b);
                }
            }

            @Override
            public Number divide(Number a, Number b) {
                if (b.intValue() == 0) {
                    if (a.intValue() == 0) {
                        return Double.NaN;
                    }

                    return a.intValue() > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                }
                int intResult = a.intValue() / b.intValue();
                // Check if the result is a whole number
                if (intResult * b.intValue() == a.intValue()) {
                    return intResult;
                } else {
                    return DOUBLE.divide(a, b);
                }
            }

            @Override
            public Number multiply(Number a, Number b) {
                Integer intR = multiplyExactWithoutException(a.intValue(), b.intValue());
                if (intR != null) {
                    return intR;
                } else {
                    return LONG.multiply(a, b);
                }
            }

            @Override
            public Number modulo(Number a, Number b) {
                if (b.intValue() == 0) {
                    return Double.NaN;
                }
                return a.intValue() % b.intValue();
            }

            @Override
            public Number power(Number a, Number b) {
                return Math.pow(a.intValue(), b.intValue());
            }

            @Override
            public int compare(Number a, Number b) {
                return Integer.compare(a.intValue(), b.intValue());
            }
        };

        private static final NumberType[] values = NumberType.values();
        private final Class<? extends Number>[] numberClasses;

        @SafeVarargs
        NumberType(Class<? extends Number>... numberClasses) {
            this.numberClasses = numberClasses;
        }

        /**
         * Type-specific add operation
         * @param a The left operand
         * @param b The right operand
         * @return The result of the operation
         * @throws UnsupportedOperationException If the operation is not supported by the type
         */
        public abstract Number add(Number a, Number b);

        /**
         * Type-specific subtract operation
         * @param a The left operand
         * @param b The right operand
         * @return The result of the operation
         * @throws UnsupportedOperationException If the operation is not supported by the type
         */
        public abstract Number subtract(Number a, Number b);

        /**
         * Type-specific divide operation
         * @param a The left operand
         * @param b The right operand
         * @return The result of the operation
         * @throws UnsupportedOperationException If the operation is not supported by the type
         */
        public abstract Number divide(Number a, Number b);

        /**
         * Type-specific multiply operation
         * @param a The left operand
         * @param b The right operand
         * @return The result of the operation
         * @throws UnsupportedOperationException If the operation is not supported by the type
         */
        public abstract Number multiply(Number a, Number b);

        /**
         * Type-specific modulo operation
         * @param a The left operand
         * @param b The right operand
         * @return The result of the operation
         * @throws UnsupportedOperationException If the operation is not supported by the type
         */
        public abstract Number modulo(Number a, Number b);

        /**
         * Type-specific power operation
         * @param a The left operand
         * @param b The right operand
         * @return The result of the operation
         * @throws UnsupportedOperationException If the operation is not supported by the type
         */
        public abstract Number power(Number a, Number b);

        /**
         * Type-specific compare operation
         * @param a The left operand
         * @param b The right operand
         * @return The result of the operation
         * @throws UnsupportedOperationException If the operation is not supported by the type
         */
        public abstract int compare(Number a, Number b);

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
