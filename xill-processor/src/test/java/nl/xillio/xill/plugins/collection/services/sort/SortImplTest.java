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

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.*;

import static nl.xillio.xill.plugins.collection.services.sort.SortImpl.Sorter;
import static nl.xillio.xill.plugins.collection.services.sort.SortImpl.getPriorityIndex;
import static nl.xillio.xill.plugins.collection.services.sort.SortImplTest.SortHelpers.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Daan Knope
 * Unit test for {@link SortImpl}
 */
public class SortImplTest {

    final String testString = "teststring";
    final Number testNumberLow = 2;
    final Number testNumberHigh = 1000;
    final List<String> testStringList = Arrays.asList("A", "C", "B");
    final List<String> reversedTestStringList = Arrays.asList("B", "C", "A");
    final List<String> sortedTestStringList = Arrays.asList("A", "B", "C");
    final List<String> reversedSortedTestStringList = Arrays.asList("C", "B", "A");
    final List<Number> testNumberList = Arrays.asList(testNumberHigh, testNumberLow);
    final List<Number> sortedTestNumberList = Arrays.asList(testNumberLow, testNumberHigh);
    final List<Boolean> testBooleanList = Arrays.asList(true, false);
    final List<Boolean> sortedTestBooleanList = Arrays.asList(false, true);
    List<Object> testNullList = new ArrayList<>();

    @BeforeTest
    private void fillNullList () {
        testNullList.add(null);
        testNullList.add(null);
    }

    public List<Object> testListSort(List<?> inputList, boolean recursive, boolean onKeys, Sorter sorter) throws Exception{
        IdentityHashMap<Object, Object> ihm = new IdentityHashMap<>();
        return (List<Object>) SortImpl.asSortedList(inputList, recursive, onKeys, sorter, ihm);
    }

    @Test
    public void testAsSortedList() throws Exception {
        final List<Object> inputList = Arrays.asList(testString, testStringList);

        // Testing all parameter settings
        List<Object> output = testListSort(inputList, false, false, Sorter.NORMAL);
        assertTrue(output.get(0) instanceof List);
        assertEquals(output.get(1), testString);

        output = testListSort(inputList, true, false, Sorter.NORMAL);
        assertEquals((List<String>) output.get(0), sortedTestStringList);
        assertEquals(output.get(1), testString);

        output = testListSort(inputList, true, true, Sorter.NORMAL);
        assertEquals(output.get(0), testString);
        assertEquals((List<String>) output.get(1), testStringList);

        output = testListSort(inputList, false, true, Sorter.NORMAL);
        assertEquals(output.get(0), testString);
        assertEquals((List<String>) output.get(1), testStringList);

        output = testListSort(inputList, false, false, Sorter.REVERSE);
        assertEquals(output.get(0), testString);
        assertEquals((List<String>) output.get(1), testStringList);

        output = testListSort(inputList, true, false, Sorter.REVERSE);
        assertEquals(output.get(0), testString);
        assertEquals((List<String>) output.get(1), reversedSortedTestStringList);

        output = testListSort(inputList, true, true, Sorter.REVERSE);
        assertEquals((List<String>) output.get(0), reversedTestStringList);
        assertEquals(output.get(1), testString);

        //sort on keys, reversed, so string and list are reversed, contents are not
        output = testListSort(inputList, false, true, Sorter.REVERSE);
        assertEquals((List<String>) output.get(0), testStringList);
        assertEquals(output.get(1), testString);

        output = testListSort(testNumberList, false, false, Sorter.NORMAL);
        assertEquals(output, sortedTestNumberList);

        output = testListSort(testNumberList, false, false, Sorter.REVERSE);
        assertEquals(output, testNumberList);

        output = testListSort(testBooleanList, false, false, Sorter.NORMAL);
        assertEquals(output, sortedTestBooleanList);

        output = testListSort(testBooleanList, false, false, Sorter.REVERSE);
        assertEquals(output, testBooleanList);

        output = testListSort(testNullList, false, false, Sorter.NORMAL);
        assertEquals(output, testNullList);
    }

    @Test
    public void testAsSortedMap() throws Exception{
        LinkedHashMap<Object, Object> output;

        output = sortMap(true, false, Sorter.NORMAL);
        assertEquals(output, getRecursivelySortedTestMap());

        output = sortMap(false, true, Sorter.NORMAL);
        assertEquals(output, getSortedOnKeysTestMap());
        
        output = sortMap(true, true, Sorter.NORMAL);
        assertEquals(output, getSortedOnKeysRecursivleyTestMap());

        output = sortMap(false, false, Sorter.REVERSE);
        assertEquals(output, getReversedTestMap());

        output = sortMap(true, false, Sorter.REVERSE);
        assertEquals(output, getRecursivelyReversedSortedTestMap());

        output = sortMap(false, true, Sorter.REVERSE);
        assertEquals(output, getReversedTestMap());

        output = sortMap(true, true, Sorter.REVERSE);
        assertEquals(output, getRecursivelyReversedOnKeysTestMap());
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
         * Helper method that returns a standardized test map with one string value and a nested map with three string values
         * @return the map
         */
        public static LinkedHashMap<Object,Object> getTestMap () {
            LinkedHashMap<Object, Object> result = new LinkedHashMap<>();
            result.put("aKey", "stringValue");

            LinkedHashMap<Object, Object> innerMap = new LinkedHashMap<>();
            innerMap.put("keyA", "valueA");
            innerMap.put("keyC", "valueC");
            innerMap.put("keyB", "valueB");
            result.put("innerMap", innerMap);
            return result;
        }

        /**
         * Helper method that returns a standardized test map with one string value and a nested map with three string values
         * @return the map
         */
        public static LinkedHashMap<Object,Object> getReversedTestMap () {
            LinkedHashMap<Object, Object> result = new LinkedHashMap<>();
            LinkedHashMap<Object, Object> innerMap = new LinkedHashMap<>();
            innerMap.put("keyA", "valueA");
            innerMap.put("keyC", "valueC");
            innerMap.put("keyB", "valueB");
            result.put("innerMap", innerMap);
            result.put("aKey", "stringValue");
            return result;
        }

        /**
         * Helper method that returns a standardized test map with one string value and a nested map with three string values
         * @return the map
         */
        public static LinkedHashMap<Object,Object> getSortedOnKeysTestMap () {
            return getTestMap();
        }

        /**
         * Helper method that returns a standardized test map with one string value and a nested map with three string values
         * @return the map
         */
        public static LinkedHashMap<Object,Object> getSortedOnKeysRecursivleyTestMap () {
            LinkedHashMap<Object, Object> result = new LinkedHashMap<>();
            LinkedHashMap<Object, Object> innerMap = new LinkedHashMap<>();
            result.put("aKey", "stringValue");
            innerMap.put("keyA", "valueA");
            innerMap.put("keyB", "valueB");
            innerMap.put("keyC", "valueC");
            result.put("innerMap", innerMap);
            return result;
        }

        /**
         * Helper method that returns a standardized test map with one string value and a nested map with three string values
         * @return the map
         */
        public static LinkedHashMap<Object,Object> getRecursivelySortedTestMap () {
            LinkedHashMap<Object, Object> result = new LinkedHashMap<>();
            LinkedHashMap<Object, Object> innerMap = new LinkedHashMap<>();
            innerMap.put("keyA", "valueA");
            innerMap.put("keyB", "valueB");
            innerMap.put("keyC", "valueC");
            result.put("innerMap", innerMap);
            result.put("aKey", "stringValue");
            return result;
        }

        /**
         * Helper method that returns a standardized test map with one string value and a nested map with three string values
         * @return the map
         */
        public static LinkedHashMap<Object,Object> getRecursivelyReversedOnKeysTestMap () {
            LinkedHashMap<Object, Object> result = new LinkedHashMap<>();
            LinkedHashMap<Object, Object> innerMap = new LinkedHashMap<>();
            innerMap.put("keyC", "valueC");
            innerMap.put("keyB", "valueB");
            innerMap.put("keyA", "valueA");
            result.put("innerMap", innerMap);
            result.put("aKey", "stringValue");
            return result;
        }

        /**
         * Helper method that returns a standardized test map with one string value and a nested map with three string values
         * @return the map
         */
        public static LinkedHashMap<Object,Object> getRecursivelyReversedSortedTestMap () {
            LinkedHashMap<Object, Object> result = new LinkedHashMap<>();
            LinkedHashMap<Object, Object> innerMap = new LinkedHashMap<>();
            result.put("aKey", "stringValue");
            innerMap.put("keyC", "valueC");
            innerMap.put("keyB", "valueB");
            innerMap.put("keyA", "valueA");
            result.put("innerMap", innerMap);
            return result;
        }

        /**
         * Helper function to run the {@link SortImpl#asSortedMap(Object, boolean, boolean, Sorter, IdentityHashMap)} with various parameters.
         * @param recursive a {@link Boolean} to indicate if the map should be recursively sorted
         * @param onKeys a {@link Boolean} to indicate if the map should be sorted on keys (values if {@code false})
         * @param sorter a {@link Sorter} object which will be used to sort
         * @return a {@link LinkedHashMap} containing the sorted objects
         */
        public static LinkedHashMap<Object, Object> sortMap(boolean recursive, boolean onKeys, Sorter sorter) {
            IdentityHashMap<Object, Object> ihm = new IdentityHashMap<>();
            return (LinkedHashMap<Object, Object>) SortImpl.asSortedMap(getTestMap(), recursive, onKeys, sorter, ihm);
        }
    }

}