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
package nl.xillio.xill.plugins.database.util;

import nl.xillio.xill.api.components.MetaExpression;
import oracle.sql.Datum;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Converts from one JDBC type to a type suited for a MetaExpression.
 *
 * @author Geert Konijnendijk
 */
public enum TypeConverter {

    /**
     * Converts a {@link Character} to a {@link String}
     */
    CHAR(Character.class) {
        @Override
        protected Object convert(Object o) {
            return String.valueOf((Character) o);
        }
    },
    /**
     * Converts a {@link Byte} to an int
     */
    BYTE(Byte.class) {
        @Override
        protected Object convert(Object o) {
            return ((Byte) o).intValue();
        }
    },
    /**
     * Converts a {@link Short} to a int
     */
    SHORT(Short.class) {
        @Override
        protected Object convert(Object o) {
            return ((Short) o).intValue();
        }
    },
    /**
     * Converts a {@link Float} to a double
     */
    FLOAT(Float.class) {
        @Override
        protected Object convert(Object o) {
            return ((Float) o).doubleValue();
        }
    },
    /**
     * Converts a {@link BigDecimal} to a double. Throws a {@link ConversionException} when the decimal is too large for a double to hold.
     */
    BIG_DECIMAL(BigDecimal.class) {
        @Override
        public Object convert(Object o) throws ConversionException {
            BigDecimal decimal = (BigDecimal) o;
            if (decimal.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) > 0) {
                throw new ConversionException("Number [" + decimal.toString() + "] is too large to be converted");
            }
            // If this number has no decimal positions, and it fits in a long value return the long value
            if (decimal.remainder(BigDecimal.ONE).equals(BigDecimal.ZERO) && decimal.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) <= 0) {
                return decimal.longValue();
            }
            return decimal.doubleValue();
        }
    },
    /**
     * Converts a {@link BigInteger} to a long. Throws a {@link ConversionException} when the integer is too long for a long to hold.
     */
    BIG_INTEGER(BigInteger.class) {
        @Override
        protected Object convert(Object o) throws ConversionException {
            BigInteger integer = (BigInteger) o;
            if (integer.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                throw new ConversionException("Number [" + integer.toString() + "] is too large to be converted");
            }
            return integer.longValue();
        }
    },
    /**
     * Convert a {@link Clob} (Character Large Object) to String.
     */
    CLOB(Clob.class) {
        @Override
        protected Object convert(Object o) throws ConversionException {
            Clob clob = (Clob) o;
            long length;
            try {
                length = clob.length();
            } catch (SQLException e) {
                throw new ConversionException(e);
            }
            if (length > Integer.MAX_VALUE) {
                throw new ConversionException("Clob is too long");
            }
            try {
                return clob.getSubString(1, (int) length);
            } catch (SQLException e) {
                throw new ConversionException(e);
            }
        }
    },
    /**
     * Converts a {@link Date} to a String representation
     */
    DATE(Date.class) {
        @Override
        protected Object convert(Object o) {
            return DEFAULT_DATE_FORMAT.format((Date) o);
        }
    },
    /**
     * Recursively converts an {@link Array} to a {@link List}
     */
    ARRAY(Array.class) {
        @Override
        protected Object convert(Object o) throws ConversionException {
            List<Object> result = new ArrayList<>();
            try {
                ResultSet array = ((Array) o).getResultSet();
                while (array.next()) {
                    result.add(convertJDBCType(array.getObject(ARRAY_RESULTSET_VALUE_INDEX)));
                }
            } catch (SQLException e) {
                throw new ConversionException(e);
            }
            return result;
        }
    },
    /**
     * Converts a {@link oracle.sql.Datum} instance to the correct jdbc type and does further conversion since it is the root of the Oracle types hierarchy.
     */
    ORACLE_DATUM(Datum.class) {
        @Override
        protected Object convert(Object o) throws ConversionException {
            try {
                return convertJDBCType(((Datum) o).toJdbc());
            } catch (SQLException e) {
                throw new ConversionException(e);
            }
        }
    };

    private static final int ARRAY_RESULTSET_VALUE_INDEX = 2;
    private static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // A convertor should be able to convert this class and all extending classes
    private Class<?> accepts;

    private TypeConverter(Class<?> accepts) {
        this.accepts = accepts;
    }

    /**
     * Convert an accepted type to a MetaExpression suited type
     *
     * @param o
     * @return
     * @throws ConversionException When a type cannot be converted
     */
    protected abstract Object convert(Object o) throws ConversionException;

    /**
     * Convert a JDBC type to a MetaExpression suited type.
     *
     * @param o
     * @return An object that can be passed to {@link MetaExpression#parseObject(Object)}
     * @throws ConversionException
     */
    public static Object convertJDBCType(Object o) throws ConversionException {
        if (o == null)
            return o;
        // Search for a suitable convertor
        for (TypeConverter convertor : values()) {
            // Convert if the given object is the same type or a super type of the object that is accepted
            if (convertor.accepts.isAssignableFrom(o.getClass())) {
                return convertor.convert(o);
            }
        }
        // No conversion necessary
        return o;
    }

    /**
     * Thrown when a value cannot be converted by the {@link TypeConverter}
     *
     * @author Geert Konijnendijk
     */
    @SuppressWarnings("javadoc")
    public static class ConversionException extends Exception {

        public ConversionException() {
            super();
        }

        public ConversionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

        public ConversionException(String message, Throwable cause) {
            super(message, cause);
        }

        public ConversionException(String message) {
            super(message);
        }

        public ConversionException(Throwable cause) {
            super(cause);
        }
    }

}