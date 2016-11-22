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
import nl.xillio.xill.webservice.exceptions.XillCompileException;
import nl.xillio.xill.webservice.exceptions.XillInvalidStateException;
import org.apache.commons.collections.MapUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Paths;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

/**
 * Tests for the {@link XillWorker}.
 *
 * @author Xillio
 */
public class XillWorkerTest extends TestUtils {

    private XillRuntime runtime;
    private XillWorker worker;

    @BeforeMethod
    public void setUp() throws Exception {
        runtime = mock(XillRuntime.class);
        worker = new XillWorker(Paths.get("test/path"), "robot.name");
        worker.setRuntime(runtime);
    }

    @Test
    public void testInitialState() throws Exception {
        assertSame(worker.getState(), XillWorkerState.NEW);
    }

    @Test
    public void testCompileStates() throws Exception {
        doAnswer(invocation -> {
            assertSame(worker.getState(), XillWorkerState.COMPILING);
            return null;
        }).when(runtime).compile(any(), any());
        worker.compile();
        assertSame(worker.getState(), XillWorkerState.IDLE);
    }

    @Test(expectedExceptions = XillInvalidStateException.class)
    public void testDoubleCompile() throws Exception {
        doNothing().when(runtime).compile(any(), any());
        worker.compile();
        worker.compile();
    }

    @Test(expectedExceptions = XillCompileException.class)
    public void testCompileErrorStates() throws Exception {
        doThrow(XillCompileException.class).when(runtime).compile(any(), any());
        worker.compile();
        assertSame(worker.getState(), XillWorkerState.COMPILATION_ERROR);
    }

    @Test
    public void testAbortStates() throws Exception {
        doNothing().when(runtime).compile(any(), any());
        worker.compile();

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
     * Calling abort when a robot is aborting should propagate the exception then return to IDLE state.
     */
    @Test(expectedExceptions = XillInvalidStateException.class)
    public void testDoubleAbortStates() throws Exception {
        doNothing().when(runtime).compile(any(), any());
        worker.compile();

        doAnswer(invocation -> {
            doAnswer(invocation1 -> {
                worker.abort();
                return null;
            }).when(runtime).abortRobot();
            worker.abort();
            return null;
        }).when(runtime).runRobot(any());

        worker.run(MapUtils.EMPTY_MAP);

        assertSame(worker.getState(), XillWorkerState.IDLE);
    }

    @Test(expectedExceptions = XillInvalidStateException.class)
    public void testAbortNewState() throws Exception {
        worker.abort();
        assertSame(worker.getState(), XillWorkerState.NEW);
    }

    @Test(expectedExceptions = XillInvalidStateException.class)
    public void testAbortIdleState() throws Exception {
        doNothing().when(runtime).compile(any(), any());
        worker.compile();
        worker.abort();
        assertSame(worker.getState(), XillWorkerState.IDLE);
    }

}