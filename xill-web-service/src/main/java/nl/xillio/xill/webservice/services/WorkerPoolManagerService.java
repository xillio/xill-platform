/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.webservice.services;

import nl.xillio.xill.webservice.model.WorkerPool;
import nl.xillio.xill.webservice.types.WorkerID;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Represents a service that manages {@link WorkerPool}: factory and reference.
 */
public interface WorkerPoolManagerService {
    /**
     * Creates new or return existing WorkerPool for given projectId.
     *
     * @param workDirectory the project directory. For the moment, it's not used as there is no support for projects yet.
     * @return the po0ol for given workDirectory that has been created or reused. For the moment, it always returns one WorkerPool as there is no support for projects yet.
     */
    WorkerPool getWorkerPool(final Path workDirectory);

    /**
     * Gets the default pool based on configuration. The worker pool is instantiated if necessary.
     *
     * @return the default worker pool for this service
     */
    WorkerPool getDefaultWorkerPool();

    /**
     * Returns existing pool for given project.
     *
     * @param projectId the project id. For the moment, it's not used as there is no support for projects yet.
     * @return the existing pool for given projectId
     */
    Optional<WorkerPool> findWorkerPool(final WorkerID projectId);

    /**
     * Gets all worker pools.
     *
     * @return the list of all worker pools
     */
    List<WorkerPool> getAllWorkerPools();
}
