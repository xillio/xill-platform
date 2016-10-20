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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.OutputHandler;
import nl.xillio.xill.api.StoppableDebugger;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Robot;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.errors.XillParsingException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link WorkerThread}
 *
 * @author Geert Konijnendijk
 */
public class WorkerThreadTest extends TestUtils {

    private BlockingQueue<MetaExpression> queue;
    private RunBulkControl control;
    private OutputHandler outputHandler;
    private RobotID robotID;
    private Debugger debugger;
    private Debugger childDebugger;
    private File robotFile;
    private WorkerRobotFactory workerRobotFactory;
    private Robot robot;

    @BeforeMethod
    public void mockObjects() throws WorkerCompileException {
        debugger = mock(Debugger.class);
        childDebugger = mock(StoppableDebugger.class);
        when(debugger.createChild()).thenReturn(childDebugger);
        queue = mock(BlockingQueue.class);
        robotFile = mock(File.class);
        control = mock(RunBulkControl.class);
        when(control.getDebugger()).thenReturn(debugger);
        when(control.getCalledRobotFile()).thenReturn(robotFile);
        outputHandler = mock(OutputHandler.class);
        robotID = mock(RobotID.class);
        robot = mock(Robot.class);
        workerRobotFactory = mock(WorkerRobotFactory.class);
    }

    /**
     * Test {@link WorkerThread#run()} under normal conditions.
     */
    @Test
    public void testRun() throws InterruptedException, WorkerCompileException {
        // mock
        when(control.shouldStop()).thenReturn(false, true);
        MetaExpression item = mockExpression(ExpressionDataType.ATOMIC);
        when(queue.poll(anyInt(), any())).thenReturn(item);
        when(debugger.shouldStop()).thenReturn(false);
        when(workerRobotFactory.construct(any(), any())).thenReturn(mock(Robot.class));

        WorkerThread workerThread = new WorkerThread(queue, control, false, workerRobotFactory);

        // run
        workerThread.run();

        // verify
        verify(control).incRunCount();
    }

    /**
     * Test {@link WorkerThread#run()} when the debugger signals to stop
     */
    @Test
    public void testRunDebuggerStop() throws InterruptedException, WorkerCompileException {
        // mock
        when(control.shouldStop()).thenReturn(false, true);
        MetaExpression item = mockExpression(ExpressionDataType.ATOMIC);
        when(queue.poll(anyInt(), any())).thenReturn(item);
        when(debugger.shouldStop()).thenReturn(true);
        when(workerRobotFactory.construct(any(), any())).thenReturn(mock(Robot.class));

        WorkerThread workerThread = new WorkerThread(queue, control, false, workerRobotFactory);

        // run
        workerThread.run();

        // verify
        verify(control).signalStop();
    }

    /**
     * Test {@link WorkerThread#run()} when the the robot could not be compiled
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*Robot error.*")
    public void testRunErrorCompile() throws InterruptedException, WorkerCompileException {
        // mock
        when(control.shouldStop()).thenReturn(false, true);
        MetaExpression item = mockExpression(ExpressionDataType.ATOMIC);
        when(queue.poll(anyInt(), any())).thenReturn(item);
        when(debugger.shouldStop()).thenReturn(false);
        when(workerRobotFactory.construct(any(), any())).thenThrow(new WorkerCompileException("Robot error", new Exception()));

        WorkerThread workerThread = new WorkerThread(queue, control, false, workerRobotFactory);

        // run
        workerThread.run();
    }

    /**
     * Test {@link WorkerThread#run()} when a {@link RobotRuntimeException} occurs while running a robot
     */
    @Test
    public void testRunRobotRuntimeException() throws InterruptedException, WorkerCompileException {
        // mock
        when(control.shouldStop()).thenReturn(false, true);
        MetaExpression item = mockExpression(ExpressionDataType.ATOMIC);
        when(queue.poll(anyInt(), any())).thenReturn(item);
        when(debugger.shouldStop()).thenReturn(false);
        RuntimeException runtimeException = new RobotRuntimeException("Error running robot");
        when(workerRobotFactory.construct(any(), any())).thenReturn(robot);
        when(robot.process(any())).thenThrow(runtimeException);

        WorkerThread workerThread = new WorkerThread(queue, control, false, workerRobotFactory);

        // run
        workerThread.run();

        // verify
        verify(debugger).handle(isA(RobotRuntimeException.class));
        verify(control).signalStop();
    }

    /**
     * Test {@link WorkerThread#run()} when an {@link Exception} occurs while running a robot
     */
    @Test
    public void testRunException() throws InterruptedException, WorkerCompileException {
        // mock
        when(control.shouldStop()).thenReturn(false, true);
        MetaExpression item = mockExpression(ExpressionDataType.ATOMIC);
        when(queue.poll(anyInt(), any())).thenReturn(item);
        when(debugger.shouldStop()).thenReturn(false);
        RuntimeException runtimeException = new RuntimeException("Error running robot");
        when(workerRobotFactory.construct(any(), any())).thenReturn(robot);
        when(robot.process(any())).thenThrow(runtimeException);

        WorkerThread workerThread = new WorkerThread(queue, control, false, workerRobotFactory);

        // run
        workerThread.run();

        // verify
        verify(debugger).handle(isA(RobotRuntimeException.class));
        verify(control).signalStop();
    }

    /**
     * Test {@link WorkerThread#run()} when the thread is interrupted
     */
    @Test
    public void testRunInterrupted() throws InterruptedException, WorkerCompileException {
        // mock
        when(control.shouldStop()).thenReturn(false, true);
        MetaExpression item = mockExpression(ExpressionDataType.ATOMIC);
        when(queue.poll(anyInt(), any())).thenThrow(InterruptedException.class);

        WorkerThread workerThread = new WorkerThread(queue, control, false, workerRobotFactory);

        // run
        workerThread.run();

        // verify
        verify(control).shouldStop();
        verifyNoMoreInteractions(control, debugger);
    }

}
