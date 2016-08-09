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

import static nl.xillio.xill.api.components.AtomicExpressionTest.AtomicExpressionTestHelperFunctions.testClosing;
import static nl.xillio.xill.api.components.AtomicExpressionTest.AtomicExpressionTestHelperFunctions.testCopied;
import static org.testng.Assert.*;

/**
 * This class tests the {@link AtomicExpression } class.
 *
 * @author Daan Knoope
 */
public class AtomicExpressionTest {

    AtomicExpression number = new AtomicExpression(3);
    AtomicExpression bool   = new AtomicExpression(true);
    AtomicExpression string = new AtomicExpression("unittesting");
    AtomicExpression empty  = new AtomicExpression(ExpressionBuilderHelper.NULL);

    @Test
    public void testGetNumberValue() throws Exception {
        assertEquals(number.getNumberValue(), 3);
        assertEquals(bool.getNumberValue(), 1);
        assertEquals(string.getNumberValue(), Double.NaN);
        assertEquals(empty.getNumberValue(), Double.NaN);
    }

    @Test
    public void testGetStringValue() throws Exception {
        assertEquals(number.getStringValue(), "3");
        assertEquals(bool.getStringValue(), "true");
        assertEquals(string.getStringValue(), "unittesting");
        assertEquals(empty.getStringValue(), "null");
    }

    @Test
    public void testGetBooleanValue() throws Exception {
        assertTrue(number.getBooleanValue());
        assertTrue(bool.getBooleanValue());
        assertTrue(string.getBooleanValue());
        assertFalse(empty.getBooleanValue());
    }

    @Test
    public void testIsNull() throws Exception {
        assertFalse(number.isNull());
        assertFalse(bool.isNull());
        assertFalse(string.isNull());
        assertTrue(empty.isNull());
    }

    @Test
    public void testGetBinaryValue() throws Exception {
        assertTrue(number.getBinaryValue() instanceof EmptyIOStream);
        assertTrue(bool.getBinaryValue() instanceof EmptyIOStream);
        assertTrue(string.getBinaryValue() instanceof EmptyIOStream);
        assertTrue(empty.getBinaryValue() instanceof EmptyIOStream);
    }

    @Test
    public void testClose() throws Exception {
        testClosing(number);
        testClosing(bool);
        testClosing(string);
        testClosing(empty);
    }

    @Test
    public void testCopy() throws Exception {
        testCopied(number);
        testCopied(bool);
        testCopied(string);
        testCopied(empty);
    }

    static class AtomicExpressionTestHelperFunctions{
        /**
         * Helper method for {@link #testClose()}. Will create copy and test if it can be closed.
         * @param atomicExpression {@link AtomicExpression} that has to be tested.
         */
        public static void testClosing(AtomicExpression atomicExpression){
            AtomicExpression copy = atomicExpression.copy();
            copy.close();
            assertFalse(copy.isOpen());
        }

        /**
         * Helper method for {@link #testCopy()}. Will create copy and test if it has the same
         * value but is a new object in memory.
         * @param atomicExpression the {@link AtomicExpression} that has to be tested.
         */
        public static void testCopied(AtomicExpression atomicExpression){
            AtomicExpression copy = atomicExpression.copy();
            assertEquals(copy, atomicExpression);  // same value
            assertFalse(copy == atomicExpression); // new object in memory
        }
    }
}