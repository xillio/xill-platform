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

import nl.xillio.xill.plugins.database.util.TypeConverter.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static nl.xillio.xill.plugins.database.util.TypeConverter.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Tests for the {@link TypeConverter}
 *
 * @author Geert Konijnendijk
 */
public class TypeConverterTest {

    private static final String TEST_STRING = "Don't panic";
    private static final double EPS = 10e-6;
    private static final int ARRAY_RESULTSET_VALUE = 2;

    /**
     * Test the conversion from {@link Byte} to int
     *
     * @throws ConversionException
     * @throws SQLException
     */
    @Test
    public void testByteConversion() throws SQLException, ConversionException {
        Byte b = (byte) 42;
        Object result = BYTE.convert(b);
        assertEquals(result, 42);
    }

    /**
     * Test conversion from {@link Short} to int
     *
     * @throws SQLException
     * @throws ConversionException
     */
    @Test
    public void testShortConversion() throws SQLException, ConversionException {
        Short s = (short) 100;
        Object result = SHORT.convert(s);
        assertEquals(result, 100);
    }

    /**
     * Test conversion from {@link Float} to double
     *
     * @throws ConversionException
     * @throws SQLException
     */
    @Test
    public void testFloatConversion() throws SQLException, ConversionException {
        Float f = 3.141f;
        Object result = FLOAT.convert(f);
        assertEquals((double) result, 3.141, EPS);
    }

    /**
     * Test conversion from {@link BigDecimal} to double under normal circumstances
     *
     * @throws ConversionException
     * @throws SQLException
     */
    @Test
    public void testBigDecimalNormal() throws SQLException, ConversionException {
        BigDecimal bd = new BigDecimal(2.718);
        Object result = BIG_DECIMAL.convert(bd);
        assertEquals((double) result, 2.718, EPS);
    }

    /**
     * Test conversion from {@link BigDecimal} to double when the decimal is larger than the possible value for double
     *
     * @throws ConversionException
     * @throws SQLException
     */
    @Test(expectedExceptions = ConversionException.class)
    public void testBigDecimalLarge() throws SQLException, ConversionException {
        BigDecimal bd = new BigDecimal("10E2048");
        BIG_DECIMAL.convert(bd);
    }

    /**
     * Test conversion from {@link BigInteger} to long under normal circumstances
     *
     * @throws ConversionException
     */
    @Test
    public void testBigIntegerNormal() throws ConversionException {
        BigInteger bi = BigInteger.valueOf(10);
        Object result = BIG_INTEGER.convert(bi);
        assertEquals((long) result, 10);
    }

    /**
     * Test conversion from {@link BigInteger} to long when the integer is larger than the possible value for long
     *
     * @throws ConversionException
     */
    @Test(expectedExceptions = ConversionException.class)
    public void testBigIntegerLarge() throws ConversionException {
        BigInteger bi = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        BIG_INTEGER.convert(bi);
    }

    /**
     * Test conversion from {@link Clob} to String under normal conditions
     *
     * @throws SQLException
     * @throws ConversionException
     */
    @Test
    public void testClobNormal() throws SQLException, ConversionException {
        // Mock
        Clob c = mockClob();

        // Run
        Object result = CLOB.convert(c);

        // Verify
        verify(c).length();
        verify(c).getSubString(1, TEST_STRING.length());

        // assert
        assertSame(result, TEST_STRING);
    }

    /**
     * Test conversion from {@link Clob} to String when the {@link Clob} is too large to fit in a String.
     *
     * @throws SQLException
     * @throws ConversionException
     */
    @Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = "Clob\\ is\\ too\\ long")
    public void testClobLarge() throws SQLException, ConversionException {
        // Mock
        Clob c = mock(Clob.class);
        when(c.length()).thenReturn(Long.MAX_VALUE);

        // Run
        Object result = CLOB.convert(c);
    }

    /**
     * Test conversion from {@link Date} to a String representation
     *
     * @throws SQLException
     * @throws ConversionException
     */
    @Test
    public void testDate() throws SQLException, ConversionException {
        // Mock
        Date d = mockDate();

        // Run
        Object result = DATE.convert(d);

        // Assert
        assertEquals(result, "2015-09-01 09:32:38");

    }

    /**
     * Test conversion from {@link Array} to {@link List}
     *
     * @throws SQLException
     * @throws ConversionException
     */
    @Test
    public void testArray() throws SQLException, ConversionException {
        // Mock

        ResultSet rs = mockResultSet();
        Array a = mockArray(rs);

        List<Integer> expectedResult = new ArrayList<>();
        expectedResult.add(1);
        expectedResult.add(2);

        // Run
        Object result = ARRAY.convert(a);

        // Verify
        verify(a).getResultSet();
        verify(rs, times(3)).next();
        verify(rs, times(2)).getObject(ARRAY_RESULTSET_VALUE);

        // Assert
        assertEquals((List<?>) result, expectedResult);
    }

    /**
     * Test if the {@link TypeConverter} converts from a certain type to the correct other type
     *
     * @throws ConversionException
     * @throws SQLException
     */
    @Test(dataProvider = "conversionTypes")
    public void testConvertJDBCType(Object input, Class<?> expectedType) throws SQLException, ConversionException {
        Object result = TypeConverter.convertJDBCType(input);
        assertTrue(expectedType.isAssignableFrom(result.getClass()), String.format("Class was %s but shoud be %s", result.getClass().getSimpleName(), expectedType.getSimpleName()));
    }

    /**
     * Mock a sample {@link Clob}
     *
     * @return
     * @throws SQLException
     */
    private Clob mockClob() throws SQLException {
        Clob c = mock(Clob.class);
        when(c.getSubString(anyLong(), anyInt())).thenReturn(TEST_STRING);
        when(c.length()).thenReturn((long) TEST_STRING.length());
        return c;
    }

    /**
     * Mock a sample date
     *
     * @return
     */
    private Date mockDate() {
        return new Date(1441092758696l);
    }

    /**
     * Mock a sample Array
     *
     * @param rs The {@link ResultSet} to return when {@link Array#getResultSet()} is called
     * @return
     * @throws SQLException
     */
    private Array mockArray(ResultSet rs) throws SQLException {
        Array a = mock(Array.class);
        when(a.getResultSet()).thenReturn(rs);
        return a;
    }

    /**
     * Mock a sample {@link ResultSet}
     *
     * @return
     * @throws SQLException
     */
    private ResultSet mockResultSet() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getObject(ARRAY_RESULTSET_VALUE)).thenReturn(1, 2);
        return rs;
    }

    /**
     * @return A 2-dimensional array with each nested array containing an Object to be converted and an expected resulting class.
     * @throws SQLException
     */
    @DataProvider(name = "conversionTypes")
    private Object[][] conversionTypes() throws SQLException {
        return new Object[][]{
                {(byte) 1, Integer.class},
                {(short) 2, Integer.class},
                {3f, Double.class},
                {BigDecimal.valueOf(5), Long.class},
                {BigDecimal.valueOf(5.1), Double.class},
                {BigInteger.valueOf(10), Long.class},
                {mockClob(), String.class},
                {mockDate(), String.class},
                {mockArray(mockResultSet()), List.class}};
    }
}
