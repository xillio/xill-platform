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
 * This class tests the {@link BooleanBehavior} class.
 *
 * @author Daan Knoope
 */
public class BooleanBehaviorTest{

    BooleanBehavior trueBehavior = new BooleanBehavior(true);
    BooleanBehavior falseBehavior = new BooleanBehavior(false);

    @Test
    public void testGetNumberValue() throws Exception {
        assertEquals(trueBehavior.getNumberValue(), 1);
        assertEquals(falseBehavior.getNumberValue(), 0);
    }

    @Test
    public void testGetStringValue() throws Exception {
        assertEquals(trueBehavior.getStringValue(), "true");
        assertEquals(falseBehavior.getStringValue(), "false");
    }

    @Test
    public void testGetBooleanValue() throws Exception {
        assertEquals(trueBehavior.getBooleanValue(), true);
        assertEquals(falseBehavior.getBooleanValue(),false);
    }

    @Test
    public void testCopy() throws Exception {
        BooleanBehavior copyTrueBehavior = trueBehavior.copy();

        assertTrue(copyTrueBehavior.getBooleanValue()); // same value
        assertTrue(trueBehavior != copyTrueBehavior);   // different object

        BooleanBehavior copyFalseBehavior = falseBehavior.copy();

        assertFalse(copyFalseBehavior.getBooleanValue()); // same value
        assertTrue(falseBehavior != copyFalseBehavior);   // different object

    }


}