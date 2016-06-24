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
package nl.xillio.xill.plugins.collection.services.duplicate;

import com.google.inject.Singleton;

import java.util.*;
import java.util.Map.Entry;

/**
 * This is the main implementation of {@link Duplicate}.
 *
 * @author Sander Visser
 */
@Singleton
public class DuplicateImpl implements Duplicate {

    @Override
    public Object duplicate(final Object input) {
        return deepCopyList(input, new IdentityHashMap<>());
    }

    @SuppressWarnings("unchecked")
    private Object deepCopyList(Object input, IdentityHashMap<Object, Object> results) {
        if (results.containsKey(input)) {
            return results.get(input);
        }

        if (input instanceof List) {
            List<Object> list = (List<Object>) input;
            List<Object> dup = copy(list);
            results.put(list, dup);

            for (int i = 0; i < dup.size(); i++) {
                Object child = dup.get(i);
                dup.set(i, deepCopyList(child, results));
            }

            return dup;
        } else if (input instanceof Map) {
            Entry<String, Object>[] sortedEntries = ((Map<String, Object>) input)
                    .entrySet()
                    .stream()
                    .toArray(Entry[]::new);

            Map<String, Object> map = new LinkedHashMap<>();
            for (Entry<String, Object> entry : sortedEntries) {
                map.put(entry.getKey(), entry.getValue());
            }

            for (String key : map.keySet()) {
                Object child = map.get(key);
                map.put(key, deepCopyList(child, results));
            }
            return map;
        }
        return input;
    }

    private List<Object> copy(List<Object> input) {
        List<Object> output = new ArrayList<>();
        for (Object o : input) {
            output.add(o);
        }
        return output;
    }
}
