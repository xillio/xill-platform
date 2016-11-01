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
package nl.xillio.xill.api.components;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * This class tests the {@link NumberBehavior} class.
 *
 * @author Daan Knoope
 */
public class NumberBehaviorTest {

    NumberBehavior number = new NumberBehavior(3);
    NumberBehavior zero = new NumberBehavior(0);
    NumberBehavior doubleNan = new NumberBehavior(Double.NaN);
    NumberBehavior floatNan = new NumberBehavior(Float.NaN);

    @Test
    public void testGetNumberValue() throws Exception {
        assertEquals(number.getNumberValue(), 3);
        assertEquals(zero.getNumberValue(), 0);
        assertTrue(Double.isNaN(doubleNan.getNumberValue().doubleValue()));
        assertTrue(Double.isNaN(floatNan.getNumberValue().doubleValue()));
    }

    @Test
    public void testGetStringValue() throws Exception {
        assertEquals(number.getStringValue(), "3");
        assertEquals(zero.getStringValue(), "0");
        assertEquals(doubleNan.getStringValue(), "NaN");
        assertEquals(floatNan.getStringValue(), "NaN");
    }

    @Test
    public void testGetBooleanValue() throws Exception {
        assertTrue(number.getBooleanValue());
        assertFalse(zero.getBooleanValue());
        assertFalse(doubleNan.getBooleanValue());
        assertFalse(floatNan.getBooleanValue());
    }

    @Test
    public void testCopy() throws Exception {
        NumberBehavior numberCopy = number.copy();
        NumberBehavior zeroCopy = zero.copy();
        NumberBehavior doubleNanCopy = doubleNan.copy();
        NumberBehavior floatNanCopy = floatNan.copy();

        // Copied the correct values
        assertEquals(numberCopy.getNumberValue(), 3);
        assertEquals(zeroCopy.getNumberValue(), 0);
        assertTrue(Double.isNaN(doubleNan.getNumberValue().doubleValue()));
        assertTrue(Double.isNaN(floatNan.getNumberValue().doubleValue()));

        // Created new object
        assertNotSame(numberCopy, number);
        assertNotSame(zeroCopy, zero);
        assertNotSame(doubleNanCopy, doubleNan);
        assertNotSame(floatNanCopy, floatNan);
    }

}
