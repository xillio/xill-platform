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

import nl.xillio.xill.webservice.exceptions.AllocateWorkerException;
import nl.xillio.xill.webservice.exceptions.BaseException;
import nl.xillio.xill.webservice.exceptions.InvalidStateException;
import nl.xillio.xill.webservice.exceptions.RobotNotFoundException;
import nl.xillio.xill.webservice.types.WorkerID;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

/**
 * Tests for the {@link WorkerPool}.
 */
public class WorkerPoolTest {

    private final String robotFQN = "test.robot";

    /**
     * Test {@link WorkerPool#allocateWorker} under normal circumstances.
     */
    @Test
    public void allocateWorker() throws BaseException {
        // mock
        Worker worker = mock(Worker.class);
        WorkerFactory factory = mock(WorkerFactory.class);
        when(factory.constructWorker(any(), anyString())).thenReturn(worker);

        // run
        WorkerPool pool = new WorkerPool(null, 3, factory);
        Worker result = pool.allocateWorker(robotFQN);

        // verify
        verify(factory).constructWorker(any(), eq(robotFQN));

        // assert
        assertSame(result, worker);
    }

    /**
     * Test {@link WorkerPool#allocateWorker} when the worker allocation fails.
     */
    @Test(expectedExceptions = AllocateWorkerException.class)
    public void allocateWorkerTestWhenAllocateFails() throws Exception {
        // mock
        WorkerFactory factory = mock(WorkerFactory.class);
        when(factory.constructWorker(any(), anyString())).thenThrow(new AllocateWorkerException(""));

        // run
        WorkerPool pool = new WorkerPool(null, 3, factory);
        pool.allocateWorker(robotFQN);
    }

    /**
     * Test {@link WorkerPool#runWorker} under normal circumstances.
     */
    @Test
    public void runWorkerTest() throws BaseException {
        // mock
        String testResult = "test result";
        Worker worker = mock(Worker.class);
        when(worker.getId()).thenReturn(new WorkerID());
        when(worker.run(any())).thenReturn(testResult);

        WorkerFactory factory = mock(WorkerFactory.class);
        when(factory.constructWorker(any(), anyString())).thenReturn(worker);

        WorkerPool pool = new WorkerPool(null, 3, factory);
        pool.allocateWorker(robotFQN);

        // run
        Object result = pool.runWorker(worker.getId(), null);

        // verify
        verify(worker).run(any());

        // assert
        assertSame(result, testResult);
    }

    /**
     * Test {@link WorkerPool#runWorker} when the worker is not found.
     */
    @Test(expectedExceptions = RobotNotFoundException.class)
    public void runWorkerTestWhenNotFound() throws BaseException {
        // Run
        WorkerPool pool = new WorkerPool(null, 3, null);
        pool.runWorker(new WorkerID(-1), null);
    }

    /**
     * Test {@link WorkerPool#stopWorker} under normal circumstances.
     */
    @Test
    public void stopWorkerTest() throws BaseException {
        // mock
        Worker worker = mock(Worker.class);
        when(worker.getId()).thenReturn(new WorkerID());
        doNothing().when(worker).abort();

        WorkerFactory factory = mock(WorkerFactory.class);
        when(factory.constructWorker(any(), anyString())).thenReturn(worker);

        // run
        WorkerPool pool = new WorkerPool(null, 3, factory);
        pool.allocateWorker(robotFQN);

        // run
        pool.stopWorker(worker.getId());

        // verify
        verify(worker).abort();
    }

    /**
     * Test {@link WorkerPool#stopWorker} when the worker is not in an expected state.
     */
    @Test(expectedExceptions = InvalidStateException.class)
    public void stopWorkerTestWhenNotRunning() throws BaseException {
        // mock
        Worker worker = mock(Worker.class);
        when(worker.getId()).thenReturn(new WorkerID());
        doThrow(new InvalidStateException("")).when(worker).abort();

        WorkerFactory factory = mock(WorkerFactory.class);
        when(factory.constructWorker(any(), anyString())).thenReturn(worker);

        // run
        WorkerPool pool = new WorkerPool(null, 3, factory);
        pool.allocateWorker(robotFQN);

        // run
        pool.stopWorker(worker.getId());
    }

    /**
     * Test {@link WorkerPool#releaseWorker} under normal circumstances.
     */
    @Test
    public void releaseWorkerTest() throws BaseException {
        // mock
        Worker worker = mock(Worker.class);
        when(worker.getId()).thenReturn(new WorkerID());
        when(worker.getState()).thenReturn(WorkerState.IDLE);

        WorkerFactory factory = mock(WorkerFactory.class);
        when(factory.constructWorker(any(), anyString())).thenReturn(worker);

        // run
        WorkerPool pool = new WorkerPool(null, 3, factory);
        pool.allocateWorker(robotFQN);

        // run
        pool.releaseWorker(worker.getId());

        // verify
        verify(worker).getState();
        verify(worker).close();
    }

    /**
     * Test {@link WorkerPool#releaseWorker} when the worker is not in an expected state.
     */
    @Test(expectedExceptions = InvalidStateException.class)
    public void releaseWorkerTestWhenInvalidState() throws BaseException {
        // mock
        Worker worker = mock(Worker.class);
        when(worker.getId()).thenReturn(new WorkerID());
        when(worker.getState()).thenReturn(WorkerState.RUNNING);

        WorkerFactory factory = mock(WorkerFactory.class);
        when(factory.constructWorker(any(), anyString())).thenReturn(worker);

        // run
        WorkerPool pool = new WorkerPool(null, 3, factory);
        pool.allocateWorker(robotFQN);

        // run
        pool.releaseWorker(worker.getId());
    }

    /**
     * Test {@link WorkerPool#releaseAllWorkers} under normal circumstances.
     */
    @Test
    public void releaseAllWorkers() throws BaseException {
        // mock
        Worker worker = mock(Worker.class);
        when(worker.getId()).thenReturn(new WorkerID());
        doNothing().when(worker).close();

        WorkerFactory factory = mock(WorkerFactory.class);
        when(factory.constructWorker(any(), anyString())).thenReturn(worker);

        // run
        WorkerPool pool = new WorkerPool(null, 3, factory);
        pool.allocateWorker(robotFQN);
        pool.releaseAllWorkers();

        // verify
        verify(worker).close();
    }
}