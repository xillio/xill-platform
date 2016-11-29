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

import nl.xillio.xill.webservice.XillProperties;
import nl.xillio.xill.webservice.model.XillWorkerPool;
import nl.xillio.xill.webservice.types.XWID;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class represents an implementation of the worker pools manager.
 */
@Service
public class XillWorkerPoolManagerServiceImpl implements XillWorkerPoolManagerService {

    XillProperties properties;

    private final Path DEFAULT_DIRECTORY;
    private Map<Path, XillWorkerPool> pools;
    private final XillWorkerPool DEFAULT_POOL;

    @Autowired
    public XillWorkerPoolManagerServiceImpl(XillProperties properties) {
        this.properties = properties;
        DEFAULT_DIRECTORY = Paths.get(properties.getWorkDirectory());
        DEFAULT_POOL = new XillWorkerPool(DEFAULT_DIRECTORY, properties.getMaxExecutors());
    }

    @Override
    public XillWorkerPool getWorkerPool(final Path workDirectory) {
        return pools.getOrDefault(workDirectory, new XillWorkerPool(workDirectory, properties.getMaxExecutors()));
    }

    @Override
    public XillWorkerPool getDefaultWorkerPool() {
        return DEFAULT_POOL;
    }

    @Override
    public Optional<XillWorkerPool> findWorkerPool(final XWID projectId) {
        throw new NotImplementedException("The 'findWorkerPool' method has not been implemented yet");
        //pools.values().stream().filter(pool -> pool.getWorkDirectory()).findAny();
    }

    @Override
    public List<XillWorkerPool> getAllWorkerPools() {
        return new LinkedList<>(pools.values());
    }
}
