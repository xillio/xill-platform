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
        doNothing().when(runtime).compile(any(), any());
        worker = new XillWorker(runtime, Paths.get("test/path"), "robot.name");
    }

    @Test
    public void testInitialState() throws Exception {
        assertSame(worker.getState(), XillWorkerState.IDLE);
    }

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
     * Calling abort when a robot is aborting should propagate the exception then return to IDLE state.
     */
    @Test(expectedExceptions = XillInvalidStateException.class)
    public void testDoubleAbortStates() throws Exception {
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
    public void testAbortIdleState() throws Exception {
        worker.abort();
        assertSame(worker.getState(), XillWorkerState.IDLE);
    }

}