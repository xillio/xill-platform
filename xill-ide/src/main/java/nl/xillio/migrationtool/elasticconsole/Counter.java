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
package nl.xillio.migrationtool.elasticconsole;

import java.util.HashMap;

/**
 * This class maps a value to an {@link Integer}. This can be used to count things.
 *
 * @param <T>
 */
public class Counter<T> extends HashMap<T, Integer> {
    private static final long serialVersionUID = -9154933899974709707L;

    /**
     * @return the value to which the specified key is mapped, or 0 if this map contains no mapping for the key.
     */
    @Override
    public Integer get(final Object key) {
        Integer value = super.get(key);

        if (value == null) {
            value = 0;
        }

        return value;
    }
}
