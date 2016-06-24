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

import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class represents an object that is able to
 *
 * @param <T> The base type of the stored objects
 */
public class MetadataExpressionPool<T> implements AutoCloseable {
    private static final Logger LOGGER = Log.get();
    private final List<T> data = new ArrayList<>();

    /**
     * Fetches a value from the pool.
     *
     * @param <C>   the type to fetch
     * @param clazz the type of object to get from the pool
     * @return the requested value or null is none was found
     * @throws ClassCastException     when the requested value is of a wrong type
     * @throws NoSuchElementException when the requested interface or class does not exist in this pool.
     */
    @SuppressWarnings("unchecked")
    public <C extends T> C get(final Class<C> clazz) {
        return (C) data.stream()
                .filter(element -> clazz.isAssignableFrom(element.getClass()))
                .findAny()
                .orElse(null);
    }

    /**
     * Stores a value in the pool.
     *
     * @param object object to store.
     */
    @SuppressWarnings("unchecked")
    public void put(final T object) {
        data.add(object);
    }

    /**
     * Returns whether the pool contains a mapping for this type.
     *
     * @param clazz the class to check the value for
     * @return true if an object was mapped else false
     */
    public boolean hasValue(final Class<? extends T> clazz) {
        return data.stream().anyMatch(element -> clazz.isAssignableFrom(element.getClass()));
    }

    /**
     * @return the number of objects stored in this pool
     */
    public int size() {
        return data.size();
    }

    /**
     * Tries to close all objects implementing the {@link AutoCloseable} interface, and
     * clears the map.
     */
    @Override
    public void close() {
        for (T closable : data) {
            if (closable instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) closable).close();
                } catch (Exception e) {
                    LOGGER.error("Exception while closing " + closable, e);
                }
            }
        }
        data.clear();
    }
}
