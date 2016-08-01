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

import nl.xillio.xill.api.io.IOStream;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * This class tests the {@link BinaryBehavior} class.
 *
 * @author Daan Knoope
 */
public class BinaryBehaviorTest {

    IOStream behaviorStream;
    BinaryBehavior behavior;

    public BinaryBehaviorTest(){
        behaviorStream = mock(IOStream.class);
        when(behaviorStream.getDescription()).thenReturn("test string");
        when(behaviorStream.hasOutputStream()).thenReturn(true);
        behavior = new BinaryBehavior(behaviorStream);
    }

    @Test (expectedExceptions = NullPointerException.class)
    public void testBinaryBehaviorNullStream(){
        new BinaryBehavior(null);
    }

    @Test
    public void testBinaryBehaviorNoDescription(){
        IOStream stream = mock(IOStream.class);
        when(stream.hasOutputStream()).thenReturn(true);
        assertEquals(new BinaryBehavior(stream).getStringValue(), "[Stream]");
    }

    @Test
    public void testGetStringValue() throws Exception {
        assertEquals(behavior.getStringValue(), "[Stream: test string]");
    }

    @Test
    public void testGetBooleanValue() throws Exception {
        assertTrue(behavior.getBooleanValue());
    }

    @Test
    public void testGetBinaryValue() throws Exception {
        assertEquals(behavior.getBinaryValue(), behaviorStream);
    }

    @Test
    public void testCopy() throws Exception {
        assertNull(behavior.copy());
    }

}