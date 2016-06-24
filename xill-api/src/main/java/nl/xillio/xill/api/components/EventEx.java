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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class allows other classes to subscribe to an event that can be fired by the {@link EventHostEx}.
 * This version is thread-safe.
 *
 * @param <T> the type of the argument passed to this event
 */
public class EventEx<T> {

    /**
     * This is a list of listeners for this event.
     */
    private List<Consumer<T>> listeners = new ArrayList<>();

    /**
     * Registers a listener to the event.
     *
     * @param listener the listener to add
     */
    public synchronized void addListener(final Consumer<T> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Cannot add null listeners.");
        }

        listeners.add(listener);
    }

    /**
     * Removes a listener from the event.
     *
     * @param listener the listener to remove
     */
    public synchronized void removeListener(final Consumer<T> listener) {
        listeners.remove(listener);
    }

    /**
     * Invokes this event with a given argument.
     *
     * @param argument an argument
     */
    protected synchronized void invoke(final T argument) {
        listeners.forEach(listener -> listener.accept(argument));
    }

    /**
     * Gets the list of registered listeners.
     *
     * @return listeners
     */
    protected synchronized List<Consumer<T>> getListeners() {
        return listeners;
    }
}
