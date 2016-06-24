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
package nl.xillio.events;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class allows other classes to subscribe to an event that can be fired by the {@link EventHost}.
 *
 * @param <T>
 */
public class Event<T> {

	/**
	 * This is a list of listeners for this event.
	 */
	protected List<Consumer<T>> listeners = new ArrayList<>();

	/**
	 * Adds a listener to the event.
	 *
	 * @param listener
	 *        the listener to add
	 */
	public void addListener(final Consumer<T> listener) {
		if (listener == null) {
			throw new NullPointerException("Cannot add null listeners.");
		}

		listeners.add(listener);
	}

	/**
	 * Removes a listener from the event.
	 *
	 * @param listener
	 *        the listener to remove
	 */
	public void removeListener(final Consumer<T> listener) {
		listeners.remove(listener);
	}

}
