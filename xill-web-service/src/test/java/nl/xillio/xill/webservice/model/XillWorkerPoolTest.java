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
package nl.xillio.xill.webservice.model;

import nl.xillio.xill.webservice.exceptions.XillAllocateWorkerException;
import nl.xillio.xill.webservice.exceptions.XillInvalidStateException;
import nl.xillio.xill.webservice.exceptions.XillNotFoundException;
import nl.xillio.xill.webservice.types.XWID;
import org.apache.commons.collections.map.HashedMap;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Tests for the {@link XillWorkerPool}.
 */
public class XillWorkerPoolTest {

    private XillWorkerPool workerPool;
    private XillWorker worker;
    private XillWorker mockWorker;

    @BeforeMethod
    public void setUp() throws Exception {
        worker = mock(XillWorker.class);
        when(worker.getId()).thenReturn(new XWID(266));
        when(worker.getState()).thenReturn(XillWorkerState.IDLE);
        doThrow(new XillInvalidStateException("")).when(worker).abort();

        workerPool = spy(new XillWorkerPool(Paths.get("test/path"), 2, null));
        doReturn(worker).when(workerPool).createWorker(anyString());

        worker = workerPool.allocateWorker("robot1.name");

        XWID id = new XWID(-2);
        mockWorker = mock(XillWorker.class);
        when(mockWorker.getId()).thenReturn(id);
        doReturn(mockWorker).when(workerPool).findWorker(id);
    }

    @Test
    public void allocateWorkerTest() throws Exception {
        // Verify
        verify(workerPool, times(2)).checkWorkerPoolSize();
        verify(workerPool, times(1)).createWorker(anyString());

        // Assert
        assertNotNull(worker);
        assertEquals(workerPool.getPoolSize(), 1);
    }

    @Test(expectedExceptions = XillAllocateWorkerException.class)
    public void allocateWorkerTestWhenAllocateFails() throws Exception {
        doThrow(new XillAllocateWorkerException("")).when(workerPool).createWorker(anyString());

        // Run
        workerPool.allocateWorker("robot2.name");
    }

    @Test
    public void runWorkerTest() throws XillInvalidStateException, XillNotFoundException {
        // Mock
        String someObject = "result";
        Map<String, Object> parameters = new HashedMap();

        when(mockWorker.run(any())).thenReturn(someObject);

        // Run
        Object result = workerPool.runWorker(mockWorker.getId(), parameters);

        // Verify
        verify(mockWorker).run(parameters);
        verify(workerPool).findWorker(mockWorker.getId());

        // Assert
        assertSame(result, someObject);
    }

    @Test(expectedExceptions = XillNotFoundException.class)
    public void runWorkerTestWhenNotFound() throws XillInvalidStateException, XillNotFoundException {
        // Run
        workerPool.runWorker(new XWID(-1), null);
    }

    @Test
    public void stopWorkerTest() throws XillInvalidStateException, XillNotFoundException {
        // Mock
        doNothing().when(mockWorker).abort();

        // Run
        workerPool.stopWorker(mockWorker.getId());

        // Verify
        verify(mockWorker).abort();
        verify(workerPool).findWorker(mockWorker.getId());
    }

    @Test(expectedExceptions = XillInvalidStateException.class)
    public void stopWorkerTestWhenNotRunning() throws XillInvalidStateException, XillNotFoundException {
        // Run
        workerPool.stopWorker(worker.getId());
    }

    @Test
    public void releaseWorkerTest() throws XillInvalidStateException, XillNotFoundException {
        // Run
        workerPool.releaseWorker(worker.getId());

        // Verify
        verify(workerPool).findWorker(worker.getId());

        // Assert
        assertEquals(workerPool.getPoolSize(), 0);
    }

    @Test(expectedExceptions = XillInvalidStateException.class)
    public void releaseWorkerTestWhenInvalidState() throws XillInvalidStateException, XillNotFoundException {
        // Mock
        when(mockWorker.getState()).thenReturn(XillWorkerState.RUNNING);

        // Run
        workerPool.releaseWorker(mockWorker.getId());
    }

    @Test
    public void releaseAllWorkers() throws XillInvalidStateException, XillNotFoundException {
        // Run
        workerPool.releaseAllWorkers();

        // Assert
        assertEquals(workerPool.getPoolSize(), 0);
    }
}