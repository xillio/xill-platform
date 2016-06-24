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
package nl.xillio.xill.plugins.document.util;

import java.util.Arrays;
import java.util.Collection;

/**
 * Utility class for requirement enforcement.
 *
 * @author Thomas Biesaart
 */
public class Require {
    private Require() throws IllegalAccessException {
        // Thou shalt not instantiate utility classes
        throw new IllegalAccessException("You cannot instantiate a utility class.");
    }

    /**
     * Require objects to be not null.
     *
     * @param objects the objects to check
     * @throws IllegalArgumentException when an object is null
     */
    public static void notNull(Object... objects) {
        if (Arrays.stream(objects).anyMatch(o -> o == null)) {
            throw new IllegalArgumentException("Validation exception: required object is null");
        }
    }

    /**
     * Requires objects to be not empty, as per definition in {@link Require#isEmpty(Object)}
     *
     * @param objects to be tested
     * @throws IllegalArgumentException when at least one of the arguments is empty.
     */
    public static void notEmpty(Object... objects) {
        Arrays.stream(objects).forEach(
                o -> {
                    if (isEmpty(o)) throw new IllegalArgumentException("ValidationException: required object is empty");
                });
    }

    private static boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        } else if (o instanceof String) {
            return ((String) o).isEmpty();
        } else if (o instanceof Collection) {
            return ((Collection<?>) o).isEmpty();
        } else {
            throw new UnsupportedOperationException("isEmpty not implemented for " + o.getClass());
        }
    }


}