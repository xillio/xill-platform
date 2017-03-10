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
package nl.xillio.xill.components.expressions.runbulk;

import nl.xillio.xill.api.Debugger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import xill.RobotLoader;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * Tests for {@link RunBulkControl}.
 *
 * @author Geert Konijnendijk
 */
public class RunBulkControlTest {

    private RunBulkControl control;

    private Debugger debugger;
    private String robotFqn = "some.robot";
    private RobotLoader loader;

    @BeforeMethod
    public void setupControl() {
        debugger = mock(Debugger.class);
        loader = mock(RobotLoader.class);
        control = new RunBulkControl(debugger, robotFqn, loader);
    }

    /**
     * Test {@link RunBulkControl#getDebugger()}.
     */
    @Test
    public void testGetDebugger() {
        assertSame(control.getDebugger(), debugger);
    }

    /**
     * Test {@link RunBulkControl#getCalledRobotFqn()}}.
     */
    @Test
    public void testGetCalledRobotFqn() {
        assertSame(control.getCalledRobotFqn(), robotFqn);
    }

    /**
     * Test {@link RunBulkControl#getLoader()}}}.
     */
    @Test
    public void testGetLoader() {
        assertSame(control.getLoader(), loader);
    }

    /**
     * Test {@link RunBulkControl#getRunCount()} and {@link RunBulkControl#incRunCount()}.
     */
    @Test
    public void testGetRunCount() {
        assertEquals(control.getRunCount(), 0);
        control.incRunCount();
        assertEquals(control.getRunCount(), 1);
        control.incRunCount();
        assertEquals(control.getRunCount(), 2);
    }

    /**
     * Test {@link RunBulkControl#shouldStop()} and {@link RunBulkControl#signalStop()}.
     */
    @Test
    public void testShouldStop() {
        assertFalse(control.shouldStop());
        control.signalStop();
        assertTrue(control.shouldStop());
    }
}
