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
package nl.xillio.xill.plugins.collection.services.sort;

import org.testng.annotations.Test;

import java.util.*;

import static nl.xillio.xill.plugins.collection.services.sort.SortImpl.Sorter;
import static nl.xillio.xill.plugins.collection.services.sort.SortImpl.getPriorityIndex;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Daan Knope
 * Unit test for {@link SortImpl}
 */
public class SortImplTest {

    final String string              = "This is also a unit test";
    final List<String> single        = Arrays.asList("This", "is", "a", "unit", "test", "!");


    public List<Object> testList(boolean recursive, boolean onKeys, Sorter sorter) throws Exception{
        IdentityHashMap<Object, Object> ihm = new IdentityHashMap<>();
        final List<Object> inputList     = Arrays.asList(string, single);
        return (List<Object>) SortImpl.asSortedList(inputList, recursive, onKeys, sorter, ihm);
    }

    @Test
    public void testAsSortedListOnKeys() throws Exception {

        // Setting up variables
        final String string              = "This is also a unit test";
        final List<String> single        = Arrays.asList("This", "is", "a", "unit", "test", "!");
        final List<String> revSingle     = Arrays.asList("!", "test", "unit", "a", "is", "This");
        final List<String> sortedSingle  = Arrays.asList("!", "This", "a", "is", "test", "unit");
        final List<String> revSortSingle = Arrays.asList("unit", "test", "is", "a", "This", "!");

        // Testing all parameter settings
        List<Object> output = testList(false, false, Sorter.NORMAL);
        assertTrue(output.get(0) instanceof List);
        assertEquals(output.get(1), string);

        output = testList(true, false, Sorter.NORMAL);
        assertTrue(output.get(0) instanceof List);
        assertEquals((List<String>) output.get(0), sortedSingle);
        assertEquals(output.get(1), string);

        output = testList(true, true, Sorter.NORMAL);
        assertTrue(output.get(1) instanceof List);
        assertEquals((List<String>) output.get(1), single);
        assertEquals(output.get(0), string);

        output = testList(false, true, Sorter.NORMAL);
        assertTrue(output.get(1) instanceof List);
        assertEquals((List<String>) output.get(1), single);
        assertEquals(output.get(0), string);

        output = testList(false, false, Sorter.REVERSE);
        assertTrue(output.get(1) instanceof List);
        assertEquals(output.get(0), string);

        output = testList(true, false, Sorter.REVERSE);
        assertTrue(output.get(1) instanceof List);
        assertEquals((List<String>) output.get(1), revSortSingle);
        assertEquals(output.get(0), string);

        output = testList(true, true, Sorter.REVERSE);
        assertTrue(output.get(0) instanceof List);
        assertEquals((List<String>) output.get(0), revSingle);
        assertEquals(output.get(1), string);

        output = testList(false, true, Sorter.REVERSE);
        assertTrue(output.get(0) instanceof List);
        assertEquals((List<String>) output.get(0), single);
        assertEquals(output.get(1), string);
    }


    @Test
    public void testGetPriorityIndex() throws Exception {
        assertEquals(getPriorityIndex(new ArrayList<String>()), 0);
        assertEquals(getPriorityIndex(new HashMap<String, Integer>()), 1);
        assertEquals(getPriorityIndex(Boolean.TRUE), 2);
        assertEquals(getPriorityIndex(new Double(9)), 3);
        assertEquals(getPriorityIndex(new Character('b')), 4);
        assertEquals(getPriorityIndex(null), 5);
    }

}