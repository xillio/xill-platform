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
import static nl.xillio.xill.plugins.collection.services.sort.SortImplTest.SortHelpers.createTestMap;
import static nl.xillio.xill.plugins.collection.services.sort.SortImplTest.SortHelpers.testMapOrder;
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
    public void testAsSortedList() throws Exception {

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
    public void testAsSortedMap() throws Exception{
        LinkedHashMap<Object, Object> output = createTestMap(false, false, Sorter.NORMAL);
        testMapOrder(output, 1,0,0,1);

        output = createTestMap(true, false, Sorter.NORMAL);
        testMapOrder(output, 1,0,1,0);

        output = createTestMap(false, true, Sorter.NORMAL);
        testMapOrder(output, 0,1,0,1);

        output = createTestMap(true, true, Sorter.NORMAL);
        testMapOrder(output, 0,1,1,0);

        output = createTestMap(false, false, Sorter.REVERSE);
        testMapOrder(output, 0,1,0,1);

        output = createTestMap(true, false, Sorter.REVERSE);
        testMapOrder(output, 0,1,0,1);

        output = createTestMap(false, true, Sorter.REVERSE);
        testMapOrder(output, 1,0,0,1);

        output = createTestMap(true, true, Sorter.REVERSE);
        testMapOrder(output, 1,0,0,1);
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

    static class SortHelpers{
        /**
         * Helper function to run the {@link SortImpl#asSortedMap(Object, boolean, boolean, Sorter, IdentityHashMap)} with various parameters.
         * @param recursive a {@link Boolean} to indicate if the map should be recursively sorted
         * @param onKeys a {@link Boolean} to indicate if the map should be sorted on keys (values if {@code false})
         * @param sorter a {@link Sorter} object which will be used to sort
         * @return a {@link LinkedHashMap} containing the sorted objects
         */
        public static LinkedHashMap<Object, Object> createTestMap(boolean recursive, boolean onKeys, Sorter sorter) {
            IdentityHashMap<Object, Object> ihm = new IdentityHashMap<>();
            LinkedHashMap<Object, Object> input = new LinkedHashMap<>();
            input.put("akey1", "value1");

            LinkedHashMap<Object, Object> innerMap = new LinkedHashMap<>();
            innerMap.put("key2", "value2");
            innerMap.put("3yek", "3eulav");
            input.put("innerMap", innerMap);

            return (LinkedHashMap<Object, Object>) SortImpl.asSortedMap(input, recursive, onKeys, sorter, ihm);
        }

        /**
         * Helper function to test that the position of all elements in the result map is correct.
         * @param output the output of {@link SortImpl#asSortedMap(Object, boolean, boolean, Sorter, IdentityHashMap)}
         * @param positionKey1 the expected position of the element: <"akey1", "value1">
         * @param positionInnerMap the expected position of the inner map
         * @param positionKey2 the expected position of the element: <"key2", "value2"> within the inner map
         * @param position3yek the expected position of the element: <"3yek", "3eulav"> within the inner map
         */
        public static void testMapOrder(Object output, int positionKey1, int positionInnerMap, int positionKey2, int position3yek){
            LinkedHashMap<Object, Object> out = (LinkedHashMap<Object, Object>) output;
            Iterator iterator = out.entrySet().iterator();
            Map.Entry<Object, Object>[] elements = new Map.Entry[2];

            elements[0] = (Map.Entry<Object, Object>) iterator.next();
            elements[1] = (Map.Entry<Object, Object>) iterator.next();

            LinkedHashMap<String, String> innerMap = (LinkedHashMap<String, String>) elements[positionInnerMap].getValue();
            Iterator innerIterator = innerMap.entrySet().iterator();
            Map.Entry<String, String>[] innerElements = new Map.Entry[2];

            innerElements[0] = (Map.Entry<String, String>) innerIterator.next();
            innerElements[1] = (Map.Entry<String, String>) innerIterator.next();

            assertTrue(elements[positionInnerMap].getValue() instanceof LinkedHashMap);
            assertEquals(elements[positionKey1].getValue(), "value1");
            assertEquals(innerElements[positionKey2].getValue(), "value2");
            assertEquals(innerElements[position3yek].getValue(), "3eulav");
        }
    }

}