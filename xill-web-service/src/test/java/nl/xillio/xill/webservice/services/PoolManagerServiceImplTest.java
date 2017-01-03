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
import nl.xillio.xill.webservice.XWSUtils;
import nl.xillio.xill.webservice.model.WorkerFactory;
import nl.xillio.xill.webservice.model.WorkerPool;
import nl.xillio.xill.webservice.types.WorkerPoolID;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * Tests for {@link PoolManagerServiceImpl}.
 *
 * @author Geert Konijnendijk
 */
public class PoolManagerServiceImplTest {

    WorkerPoolManagerService poolManagerService;

    @BeforeMethod
    public void setup() {
        WorkerFactory factory = mock(WorkerFactory.class);

        WebServiceProperties properties = new WebServiceProperties();
        poolManagerService = new PoolManagerServiceImpl(properties, factory);
    }

    /**
     * Test for {@link PoolManagerServiceImpl#getWorkerPool(Path)} and {@link PoolManagerServiceImpl#getDefaultWorkerPool()}.
     */
    @Test
    public void testGetWorkerPool() {
        WorkerPool pool = poolManagerService.getWorkerPool(Paths.get(XWSUtils.getPresentWorkingDirectory()));

        assertSame(pool, poolManagerService.getDefaultWorkerPool());
    }

    /**
     * Test for {@link PoolManagerServiceImpl#findWorkerPool(WorkerPoolID)}, which has not been implemented.
     */
    @Test
    public void testFindWorkerPool() {
        WorkerPool pool = poolManagerService.getWorkerPool(Paths.get(XWSUtils.getPresentWorkingDirectory()));

        Optional<? extends WorkerPool> result = poolManagerService.findWorkerPool(pool.getId());

        assertSame(pool, result.get());
    }

    /**
     * Test for {@link PoolManagerServiceImpl#getAllWorkerPools()} when a single pool is present.
     */
    @Test
    public void testGetAllWorkerPoolsSingle() {
        List<? extends WorkerPool> pools = poolManagerService.getAllWorkerPools();

        assertEquals(pools.size(), 1, "Only one pool should be created by default");

        assertSame(pools.get(0), poolManagerService.getDefaultWorkerPool());
    }

    /**
     * Test for {@link PoolManagerServiceImpl#getAllWorkerPools()} when multiple pools are present.
     */
    @Test
    public void testGetAllWorkerPoolsMultiple() {
        WorkerPool pool = poolManagerService.getWorkerPool(Paths.get("some/nonexistant/path"));

        List<? extends WorkerPool> pools = poolManagerService.getAllWorkerPools();

        assertEquals(pools.size(), 2, "One additional worker pool should have been created");
        assertEqualsNoOrder(pools.toArray(), new WorkerPool[]{pool, poolManagerService.getDefaultWorkerPool()});
    }
}
