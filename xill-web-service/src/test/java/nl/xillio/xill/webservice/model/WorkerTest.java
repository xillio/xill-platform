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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.webservice.exceptions.*;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.concurrent.ConcurrentRuntimeException;
import org.apache.commons.pool2.ObjectPool;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

/**
 * Tests for the {@link Worker}.
 *
 * @author Xillio
 */
public class WorkerTest extends TestUtils {

    private Runtime runtime;
    private Worker worker;

    private ObjectPool<Runtime> runtimePool;

    @BeforeMethod
    public void setUp() throws Exception {
        runtime = mock(Runtime.class);
        doNothing().when(runtime).compile(any(), any());
        runtimePool = mock(ObjectPool.class);
        when(runtimePool.borrowObject()).thenReturn(runtime);
        worker = new Worker(Paths.get("test/path"), "robot.name", runtimePool);

    }

    /**
     * Test {@link Worker#Worker(Path, String, ObjectPool)} when borrowing a runtime
     * from the pool fails.
     */
    @Test(expectedExceptions = AllocateWorkerException.class)
    public void testPoolBorrowError() throws Exception {
        when(runtimePool.borrowObject()).thenThrow(Exception.class);
        worker = new Worker(Paths.get("test/path"), "robot.name", runtimePool);
    }

    /**
     * A worker cannot be created if the robot cannot compile.
     */
    @Test(expectedExceptions = CompileException.class)
    public void testCompileError() throws Exception {
        doThrow(CompileException.class).when(runtime).compile(any(), any());
        worker = new Worker(Paths.get("test/path"), "robot.name", runtimePool);
    }

    /**
     * A new worker should be in {@link WorkerState#IDLE}.
     */
    @Test
    public void testInitialState() throws Exception {
        assertSame(worker.getState(), WorkerState.IDLE);
    }

    /**
     * Checks that the worker is in {@link WorkerState#RUNNING} when run,
     * then returns to {@link WorkerState#IDLE}.
     */
    @Test
    public void testRunningState() throws Exception {
        doAnswer(invocation -> {
            assertSame(worker.getState(), WorkerState.RUNNING);
            return null;
        }).when(runtime).runRobot(any());
        worker.run(MapUtils.EMPTY_MAP);
        assertSame(worker.getState(), WorkerState.IDLE);
    }

    /**
     * If an exception happened during recompilation of a robot, an exception should be thrown
     * and the worker should fall in {@link WorkerState#RUNTIME_ERROR} state.
     */
    @Test(expectedExceptions = InvalidStateException.class)
    public void testRunTimeError() throws Exception {
        doThrow(ConcurrentRuntimeException.class).when(runtime).runRobot(MapUtils.EMPTY_MAP);

        try {
            worker.run(MapUtils.EMPTY_MAP);
        } finally {
            assertSame(worker.getState(), WorkerState.RUNTIME_ERROR);
        }
    }

    /**
     * Running a worker if it is in {@link WorkerState#RUNTIME_ERROR} should throw an exception,
     * and leave it in its state.
     */
    @Test(expectedExceptions = InvalidStateException.class)
    public void testNoRunIfErrorState() throws Exception {
        doThrow(ConcurrentRuntimeException.class).when(runtime).runRobot(MapUtils.EMPTY_MAP);
        try {
            worker.run(MapUtils.EMPTY_MAP);
        } catch (InvalidStateException e) {
            worker.run(MapUtils.EMPTY_MAP);
        } finally {
            assertSame(worker.getState(), WorkerState.RUNTIME_ERROR);
        }
    }

    /**
     * Calling abort when a worker is running should set its state to {@link WorkerState#ABORTING},
     * then return the state to {@link WorkerState#IDLE}.
     */
    @Test
    public void testAbortStates() throws Exception {
        doAnswer(invocation -> {
            doAnswer(invocation1 -> {
                assertSame(worker.getState(), WorkerState.ABORTING);
                return null;
            }).when(runtime).abortRobot();
            worker.abort();
            return null;
        }).when(runtime).runRobot(any());

        worker.run(MapUtils.EMPTY_MAP);

        assertSame(worker.getState(), WorkerState.IDLE);
    }

    /**
     * A robot not running should throw an exception when aborted.
     */
    @Test(expectedExceptions = InvalidStateException.class)
    public void testAbortIdleState() throws Exception {
        worker.abort();
        assertSame(worker.getState(), WorkerState.IDLE);
    }

    /**
     * Test {@link Worker#abort()} when aborting fails.
     */
    @Test(expectedExceptions = RobotAbortException.class)
    public void testAbortFail() throws Exception {
        doThrow(RobotAbortException.class).when(runtime).abortRobot();
        doAnswer(a -> {
            worker.abort();
            return null;
        }).when(runtime).runRobot(anyMap());

        worker.run(MapUtils.EMPTY_MAP);
    }

    /**
     * Test {@link Worker#abort()} when aborting fails and invalidating the runtime fails.
     */
    @Test(expectedExceptions = PoolFailureException.class)
    public void testInvalidationFail() throws Exception {
        doThrow(RobotAbortException.class).when(runtime).abortRobot();
        doThrow(Exception.class).when(runtimePool).invalidateObject(any());
        doAnswer(a -> {
            worker.abort();
            return null;
        }).when(runtime).runRobot(anyMap());

        worker.run(MapUtils.EMPTY_MAP);
    }

    /**
     * Test {@link Worker#close()} under normal circumstances.
     */
    @Test
    public void testClose() throws Exception {
        worker.close();

        verify(runtimePool).returnObject(runtime);
    }

    /**
     * Test that a running robot is aborted before a {@link Worker#close()} is called.
     */
    @Test
    public void testCloseWhileRunning() throws InvalidStateException {
        doAnswer(a -> {
            worker.close();
            return null;
        }).when(runtime).runRobot(anyMap());

        worker.run(MapUtils.EMPTY_MAP);

        verify(runtime).abortRobot();
    }

    /**
     * Test that a runtime is invalidated if a robot could not be aborted in time.
     */
    @Test
    public void testCloseAbortFail() throws Exception {
        doAnswer(a -> {
            worker.close();
            return null;
        }).when(runtime).runRobot(anyMap());

        doAnswer(a -> {
            throw new RobotAbortException("Test exception", null);
        }).when(runtime).abortRobot();

        worker.run(MapUtils.EMPTY_MAP);

        verify(runtimePool).borrowObject();
        verify(runtimePool).invalidateObject(runtime);
        verifyNoMoreInteractions(runtimePool);
    }

}
