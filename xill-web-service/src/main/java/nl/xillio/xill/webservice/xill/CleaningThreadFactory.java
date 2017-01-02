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
package nl.xillio.xill.webservice.xill;

import nl.xillio.xill.api.XillThreadFactory;

/**
 * Implementation of a {@link XillThreadFactory}. This implementation
 * adds all threads to a single {@link ThreadGroup}. The {@link ThreadGroup} is
 * {@link ThreadGroup#interrupt() interrupted} when this {@link XillThreadFactory} is
 * {@link XillThreadFactory#close() closed}.
 *
 * @author Geert Konijnendijk
 */
public class CleaningThreadFactory implements XillThreadFactory {

    private static final String THREAD_GROUP_NAME = "Xill Webservice Thread Factory";

    private final ThreadGroup group;

    /**
     * Creates a new factory with the default {@link ThreadGroup} name.
     */
    public CleaningThreadFactory() {
        this(THREAD_GROUP_NAME);
    }

    /**
     * Creates a new factory.
     * @param name rhe name of the {@link ThreadGroup}
     */
    public CleaningThreadFactory(String name) {
        group = new ThreadGroup(name);
        group.setDaemon(true);
    }

    @Override
    public Thread create(Runnable target, String name) {
        return new Thread(group, target, name);
    }

    @Override
    public void close() throws Exception {
        group.interrupt();
        // This method does not destroy the group since there is no guarantee all threads have stopped
    }
}
