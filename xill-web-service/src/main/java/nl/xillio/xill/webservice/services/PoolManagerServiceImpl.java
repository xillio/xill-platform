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
import nl.xillio.xill.webservice.types.WorkerID;
import org.apache.commons.lang3.NotImplementedException;
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
    private final Map<Path, WorkerPool> pools = new HashMap<>();
    private final WorkerPool defaultPool;
    private final WorkerFactory workerFactory;

    @Autowired
    public PoolManagerServiceImpl(WebServiceProperties properties, WorkerFactory workerFactory) {
        this.properties = properties;
        this.workerFactory = workerFactory;
        defaultDirectory = Paths.get(properties.getWorkDirectory());
        defaultPool = new WorkerPool(defaultDirectory, properties.getMaxExecutors(), workerFactory);
    }

    @Override
    public WorkerPool getWorkerPool(final Path workDirectory) {
        return pools.getOrDefault(workDirectory, new WorkerPool(workDirectory, properties.getMaxExecutors(), workerFactory));
    }

    @Override
    public WorkerPool getDefaultWorkerPool() {
        return defaultPool;
    }

    @Override
    public Optional<WorkerPool> findWorkerPool(final WorkerID projectId) {
        throw new NotImplementedException("The 'findWorkerPool' method has not been implemented yet");
    }

    @Override
    public List<WorkerPool> getAllWorkerPools() {
        return new LinkedList<>(pools.values());
    }
}
