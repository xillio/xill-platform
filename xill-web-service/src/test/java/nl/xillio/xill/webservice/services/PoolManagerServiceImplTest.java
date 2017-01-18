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
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for the {@link PoolManagerServiceImpl} class.
 *
 * @author Xillio
 */
public class PoolManagerServiceImplTest {

    @Test
    public void testGetDefaultWorkerPool() throws Exception {
        WorkerFactory factory = mock(WorkerFactory.class);
        WebServiceProperties properties = new WebServiceProperties();

        PoolManagerServiceImpl service = new PoolManagerServiceImpl(properties, factory);

        WorkerPool defaultPool = service.getDefaultWorkerPool();
        assertNotNull(defaultPool);
    }
}