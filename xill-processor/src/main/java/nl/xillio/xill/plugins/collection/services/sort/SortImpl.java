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

import com.google.inject.Singleton;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * This is the main implementation of {@link Sort}
 *
 * @author Sander Visser
 */
@Singleton
public class SortImpl implements Sort {

    @Override
    public Object asSorted(final Object input, final boolean recursive, final boolean onKeys, final boolean reverse) {
        Sorter sorter = reverse ? Sorter.REVERSE : Sorter.NORMAL;
        return asSorted(input, recursive, onKeys, sorter, new IdentityHashMap<>());
    }

    public static Object asSorted(final Object input, final boolean recursive, final boolean onKeys, final Sorter sorter, final Map<Object, Object> results) {
        if (results.containsKey(input)) {
            return results.get(input);
        }

        if (input instanceof List) {
            return asSortedList(input, recursive, onKeys, sorter, results);
        } else if (input instanceof Map) {
            return asSortedMap(input, recursive, onKeys, sorter, results);
        }

        return input;
    }

    public static Object asSortedList(final Object input, final boolean recursive, final boolean onKeys, Sorter sorter, final Map<Object, Object> results){
        @SuppressWarnings("unchecked")
        List<?> list = (List<?>) input;
        List<Object> sorted = list.stream().sorted((a,b) -> {
            if(onKeys){ // comparing on keys in a list should always return the first key
                return sorter.isReverse() ? -1 : 0;
            } else
                return sorter.compare(a,b);}).collect(Collectors.toList());
        results.put(list, sorted);

        // Sort recursive
        if (recursive) {
            for (int i = 0; i < sorted.size(); i++) {
                Object child = sorted.get(i);
                sorted.set(i, asSorted(child, true, onKeys, sorter, results));
            }
        }

        return sorted;
    }

    public static Object asSortedMap(final Object input, final boolean recursive, final boolean onKeys, Sorter sorter, final Map<Object, Object> results){
        // Sort the map by extracting single entries and sorting them either by key or
        @SuppressWarnings("unchecked")
        Entry<String, Object>[] sortedEntries = ((Map<String, Object>) input)
                .entrySet()
                .stream()
                .sorted((a, b) -> {
                    if (onKeys) {
                        return sorter.compare(a.getKey(), b.getKey());
                    }
                    return sorter.compare(a.getValue(), b.getValue());
                })
                .toArray(Entry[]::new);

        Map<String, Object> map = new LinkedHashMap<>();
        for (Entry<String, Object> entry : sortedEntries) {
            map.put(entry.getKey(), entry.getValue());
        }

        // Sort recursive
        if (recursive) {
            for (Map.Entry<String,Object> entry : map.entrySet()) {
                map.put(entry.getKey(), asSorted(entry.getValue(), true, onKeys, sorter, results));
            }
        }

        return map;
    }

    //returns the priority of the type of the input.
    public static int getPriorityIndex(final Object object) {

        int index;
        if (object instanceof List) {
            index = 0;
        }
        else if (object instanceof Map) {
            index = 1;
        }
        else if (object instanceof Boolean) {
            index = 2;
        }
        else if (object instanceof Number) {
            index = 3;
        }
        else if (object != null) {
            index = 4;
        }
        else{
            index = 5;
        }

        return index;
    }

    public static class Sorter implements Comparator<Object> {
        public static final Sorter NORMAL = new Sorter(false);
        public static final Sorter REVERSE = new Sorter(true);
        private final boolean reverseOrder;

        private Sorter(final boolean reverseOrder) {
            this.reverseOrder = reverseOrder;
        }

        @Override
        @SuppressWarnings({"unchecked", "squid:S1166"})
        public int compare(final Object objectA, final Object objectB) {
            int priorityA = getPriorityIndex(objectA);
            int priorityB = getPriorityIndex(objectB);
            int result = 0;
            if (priorityA != priorityB) {
                return reverseOrder ? priorityB - priorityA : priorityA - priorityB;
            }

            if (objectA instanceof Collection) {
                result = ((Collection<?>) objectA).size() - ((Collection<?>) objectB).size();
            }

            if (objectA instanceof Number) {
                Number numberA = (Number) objectA;
                Number numberB = (Number) objectB;

                result = Double.compare(numberA.doubleValue(), numberB.doubleValue());
            }
            if (objectA instanceof Boolean) {
                boolean booleanA = (boolean) objectA;
                boolean booleanB = (boolean) objectB;
                result = Boolean.compare(booleanA, booleanB);
            }
            if (objectA instanceof Entry) {
                result = ((Entry<String, Object>) objectA).getValue().toString().compareTo(((Entry<String, Object>) objectB).getValue().toString());
            }
            if (objectA instanceof String) {
                result = objectA.toString().compareTo(objectB.toString());
            }

            if (reverseOrder) {
                result = -result;
            }

            return result;
        }

        public boolean isReverse(){
            return reverseOrder;
        }

    }
}
