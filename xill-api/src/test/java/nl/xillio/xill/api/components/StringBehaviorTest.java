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

import static java.lang.Double.NaN;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * This class tests the {@link StringBehavior} class.
 *
 * @author Daan Knoope
 */
public class StringBehaviorTest {
    StringBehavior[] stringBehaviors;
    Double[] numberValues;
    String[] stringValues;
    boolean[] booleanValues;

    public StringBehaviorTest() {
        stringValues = new String[] {"test string", "", "0.0", "0", "null", "false"};
        numberValues = new Double[] {NaN, NaN, 0D, 0D, NaN, NaN};
        booleanValues = new boolean[] {true, false, true, false, false, false};

        stringBehaviors = new StringBehavior[3];
        for (int i = 0; i < stringBehaviors.length; i++)
            stringBehaviors[i] = new StringBehavior(stringValues[i]);
    }

    @Test
    public void testGetNumberValue() throws Exception {
        for (int i = 0; i < stringBehaviors.length; i++) {
            assertEquals(stringBehaviors[i].getNumberValue(), numberValues[i]);
        }
    }

    @Test
    public void testGetStringValue() throws Exception {
        for (int i = 0; i < stringBehaviors.length; i++) {
            assertEquals(stringBehaviors[i].getStringValue(), stringValues[i]);
        }
    }

    @Test
    public void testGetBooleanValue() throws Exception {
        for (int i = 0; i < stringBehaviors.length; i++) {
            assertEquals(stringBehaviors[i].getBooleanValue(), booleanValues[i]);
        }
    }

    @Test
    public void testCopy() throws Exception {
        for (int i = 0; i < stringBehaviors.length; i++) {
            StringBehavior copy = stringBehaviors[i].copy();
            assertEquals(stringBehaviors[i].getStringValue(), copy.getStringValue()); // same value
            assertTrue(stringBehaviors[i] != copy); // different objects
        }
    }
}