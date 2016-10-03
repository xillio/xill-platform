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
package nl.xillio.xill.api;

/**
 * Used to create threads that can be used by plugins, while being able to track threads.
 *
 * When a thread created using this factory is interrupted, this thread <b>must</b>
 * stop.
 *
 * The {@link XillThreadFactory} is injected into plugins and should be used by plugins
 * to create their threads. Plugins should not call any constructor of {@link Thread}
 * directly, except when there are very good reasons to do so.
 *
 * Implementing classes can create custom ways of starting, tracking and stopping threads.
 *
 * @author Geert Konijnendijk
 */
public interface XillThreadFactory extends AutoCloseable {

    /**
     * Create a new {@link Thread} from the given runnable.
     * @param runnable The runnable to create the {@link Thread} from
     * @param name An informative name reflecting what the thread is used for
     * @return A new {@link Thread}
     */
    Thread create(Runnable runnable, String name);

}
