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

import java.util.List;
import java.util.function.Consumer;

/**
 * This class represents an {@link Event} with raised permissions
 *
 * @param <T>
 */
public class EventHost<T> {
	private final Event<T> event;

	/**
	 * Create a new {@link EventHost}
	 */
	public EventHost() {
		this.event = new Event<>();
	}

	/**
	 * Invoke this event with a given argument.
	 *
	 * @param argument
	 *        an argument
	 */
	public void invoke(final T argument) {
		event.listeners.forEach(listener -> listener.accept(argument));
	}

	/**
	 * Gets a list of listeners.
	 *
	 * @return listeners
	 */
	public List<Consumer<T>> getListeners() {
		return event.listeners;
	}

	/**
	 * Returns the event.
	 *
	 * @return the event
	 */
	public Event<T> getEvent() {
		return event;
	}
}
