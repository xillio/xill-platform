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
package nl.xillio.xill.webservice.services;

import nl.xillio.xill.webservice.model.WorkerPool;

/**
 * Represents a service that manages {@link WorkerPool}: factory and reference.
 */
@SuppressWarnings("squid:S1609") // not meant to be a functional interface, represents a service
public interface WorkerPoolManagerService {

    /**
     * Gets the default pool based on configuration. The worker pool is instantiated if necessary.
     *
     * @return the default worker pool for this service
     */
    WorkerPool getDefaultWorkerPool();
}
