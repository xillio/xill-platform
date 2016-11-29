/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.webservice.model;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.webservice.exceptions.XillCompileException;
import nl.xillio.xill.webservice.exceptions.XillInvalidStateException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.concurrent.ConcurrentRuntimeException;
import org.apache.commons.pool2.ObjectPool;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Paths;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

/**
 * Tests for the {@link XillWorker}.
 *
 * @author Xillio
 */
public class XillWorkerTest extends TestUtils {

    private XillRuntime runtime;
    private XillWorker worker;

    private ObjectPool<XillRuntime> runtimePool;

    @BeforeMethod
    public void setUp() throws Exception {
        runtime = mock(XillRuntime.class);
        doNothing().when(runtime).compile(any(), any());
        runtimePool = mock(ObjectPool.class);
        when(runtimePool.borrowObject()).thenReturn(runtime);
        worker = new XillWorker(Paths.get("test/path"), "robot.name", runtimePool);

    }

    /**
     * A worker cannot be created if the robot cannot compile.
     */
    @Test(expectedExceptions = XillCompileException.class)
    public void testCompileError() throws Exception {
        doThrow(XillCompileException.class).when(runtime).compile(any(), any());
        worker = new XillWorker(Paths.get("test/path"), "robot.name", runtimePool);
    }

    /**
     * A new worker should be in {@link XillWorkerState#IDLE}.
     */
    @Test
    public void testInitialState() throws Exception {
        assertSame(worker.getState(), XillWorkerState.IDLE);
    }

    /**
     * Checks that the worker is in {@link XillWorkerState#RUNNING} when run,
     * then returns to {@link XillWorkerState#IDLE}.
     */
    @Test
    public void testRunningState() throws Exception {
        doAnswer(invocation -> {
            assertSame(worker.getState(), XillWorkerState.RUNNING);
            return null;
        }).when(runtime).runRobot(any());
        worker.run(MapUtils.EMPTY_MAP);
        assertSame(worker.getState(), XillWorkerState.IDLE);
    }

    /**
     * If an exception happened during recompilation of a robot, an exception should be thrown
     * and the worker should fall in {@link XillWorkerState#RUNTIME_ERROR} state.
     */
    @Test(expectedExceptions = XillInvalidStateException.class)
    public void testRunTimeError() throws Exception {
        doThrow(ConcurrentRuntimeException.class).when(runtime).runRobot(MapUtils.EMPTY_MAP);

        try {
            worker.run(MapUtils.EMPTY_MAP);
        } finally {
            assertSame(worker.getState(), XillWorkerState.RUNTIME_ERROR);
        }
    }

    /**
     * Running a worker if it is in {@link XillWorkerState#RUNTIME_ERROR} should throw an exception,
     * and leave it in its state.
     */
    @Test(expectedExceptions = XillInvalidStateException.class)
    public void testNoRunIfErrorState() throws Exception {
        doThrow(ConcurrentRuntimeException.class).when(runtime).runRobot(MapUtils.EMPTY_MAP);
        try {
            worker.run(MapUtils.EMPTY_MAP);
        } catch (XillInvalidStateException e) {
            worker.run(MapUtils.EMPTY_MAP);
        } finally {
            assertSame(worker.getState(), XillWorkerState.RUNTIME_ERROR);
        }
    }

    /**
     * Calling abort when a worker is running should set its state to {@link XillWorkerState#ABORTING},
     * then return the state to {@link XillWorkerState#IDLE}.
     */
    @Test
    public void testAbortStates() throws Exception {
        doAnswer(invocation -> {
            doAnswer(invocation1 -> {
                assertSame(worker.getState(), XillWorkerState.ABORTING);
                return null;
            }).when(runtime).abortRobot();
            worker.abort();
            return null;
        }).when(runtime).runRobot(any());

        worker.run(MapUtils.EMPTY_MAP);

        assertSame(worker.getState(), XillWorkerState.IDLE);
    }

    /**
     * A robot not running should throw an exception when aborted.
     */
    @Test(expectedExceptions = XillInvalidStateException.class)
    public void testAbortIdleState() throws Exception {
        worker.abort();
        assertSame(worker.getState(), XillWorkerState.IDLE);
    }

}
