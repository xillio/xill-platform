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

import java.io.Serializable;
import java.util.Iterator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;


public class MetadataExpressionPoolTest {

    @Test
    public void testGetByInterface() throws Exception {
        MetadataExpressionPool<Object> pool = new MetadataExpressionPool<>();
        TestObject object = new TestObject();

        assertFalse(pool.hasValue(Serializable.class));
        assertFalse(pool.hasValue(TestObject.class));

        pool.put(object);

        assertTrue(pool.hasValue(Serializable.class));
        assertTrue(pool.hasValue(TestObject.class));
        assertSame(pool.get(Serializable.class), object);
        assertSame(pool.get(TestObject.class), object);
    }

    @Test
    public void testClose() {
        MetadataExpressionPool<Object> pool = new MetadataExpressionPool<>();
        TestObject obj = mock(TestObject.class);

        assertEquals(pool.size(), 0);
        pool.put(obj);
        assertEquals(pool.size(), 1);

        pool.close();
        assertEquals(pool.size(), 0);

        verify(obj).close();
    }

    @Test
    public void testGetIterator() {
        MetadataExpressionPool<Object> pool = new MetadataExpressionPool<>();
        Double d = 5.0;
        pool.put(d);
        Integer i = 7;
        pool.put(i);

        // Run
        Iterator<Object> iterator = pool.iterator();

        // Assert
        assertSame(iterator.next(), d);
        assertEquals(iterator.next(), i);
        assertFalse(iterator.hasNext());
    }

    class TestObject implements Serializable, AutoCloseable {

        @Override
        public void close() {

        }
    }
}
