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

import nl.xillio.xill.webservice.WebServiceProperties;
import nl.xillio.xill.webservice.model.WorkerFactory;
import nl.xillio.xill.webservice.model.WorkerPool;
import nl.xillio.xill.webservice.types.WorkerPoolID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class represents an implementation of the worker pools manager.
 */
@Service
public class PoolManagerServiceImpl implements WorkerPoolManagerService {

    WebServiceProperties properties;

    private final Path defaultDirectory;
    private final Map<String, WorkerPool> pools = new HashMap<>();
    private final WorkerPool defaultPool;
    private final WorkerFactory workerFactory;

    @Autowired
    public PoolManagerServiceImpl(WebServiceProperties properties, WorkerFactory workerFactory) {
        this.properties = properties;
        this.workerFactory = workerFactory;
        defaultDirectory = Paths.get(properties.getWorkDirectory());
        defaultPool = new WorkerPool(defaultDirectory, properties.getMaxExecutors(), workerFactory);
        pools.put(pathToIdentifier(defaultDirectory), defaultPool);
    }

    @Override
    public WorkerPool getWorkerPool(final Path workDirectory) {
        // Convert to absolute path and string to always have the same path
        String poolKey = pathToIdentifier(workDirectory);
        WorkerPool pool = pools.get(poolKey);
        if (pool == null) {
            pool = new WorkerPool(workDirectory, properties.getMaxExecutors(), workerFactory);
            pools.put(poolKey, pool);
        }
        return pool;
    }

    @Override
    public WorkerPool getDefaultWorkerPool() {
        return defaultPool;
    }

    @Override
    public Optional<WorkerPool> findWorkerPool(final WorkerPoolID projectId) {
        return pools.values().stream().filter(p -> p.getId().equals(projectId)).findFirst();
    }

    @Override
    public List<WorkerPool> getAllWorkerPools() {
        return new LinkedList<>(pools.values());
    }

    /**
     * Convert a path to a string that can be used to identify pools.
     *
     * @return A normalized absolute path
     */
    private String pathToIdentifier(Path path) {
        return path.toAbsolutePath().normalize().toString();
    }
}
