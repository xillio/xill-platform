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

    @BeforeMethod
    public void mockObjects() {
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
        workerRobotFactory = mock(WorkerRobotFactory.class);
    }

    /**
     * Test {@link WorkerThread#run()} under normal conditions.
     */
    @Test
    public void testRun() throws InterruptedException, IOException, XillParsingException {
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
    public void testRunDebuggerStop() throws InterruptedException, IOException, XillParsingException {
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
     * Test {@link WorkerThread#run()} when the robot file is wrong
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*Wrong robot file.*")
    public void testRunWrongRobotFile() throws InterruptedException, IOException, XillParsingException {
        // mock
        when(control.shouldStop()).thenReturn(false, true);
        MetaExpression item = mockExpression(ExpressionDataType.ATOMIC);
        when(queue.poll(anyInt(), any())).thenReturn(item);
        when(debugger.shouldStop()).thenReturn(false);
        when(workerRobotFactory.construct(any(), any())).thenThrow(new IOException("Wrong robot file"));

        WorkerThread workerThread = new WorkerThread(queue, control, false, workerRobotFactory);

        // run
        workerThread.run();
    }

    /**
     * Test {@link WorkerThread#run()} when the the robot could not be compiled
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*Error compiling robot.*")
    public void testRunErrorCompile() throws InterruptedException, IOException, XillParsingException {
        // mock
        when(control.shouldStop()).thenReturn(false, true);
        MetaExpression item = mockExpression(ExpressionDataType.ATOMIC);
        when(queue.poll(anyInt(), any())).thenReturn(item);
        when(debugger.shouldStop()).thenReturn(false);
        when(workerRobotFactory.construct(any(), any())).thenThrow(new XillParsingException("Error compiling robot", 0, robotID));

        WorkerThread workerThread = new WorkerThread(queue, control, false, workerRobotFactory);

        // run
        workerThread.run();
    }

    /**
     * Test {@link WorkerThread#run()} when an exception occurs while running a robot
     */
    @Test
    public void testRunRobotException() throws InterruptedException, IOException, XillParsingException {
        // mock
        when(control.shouldStop()).thenReturn(false, true);
        MetaExpression item = mockExpression(ExpressionDataType.ATOMIC);
        when(queue.poll(anyInt(), any())).thenReturn(item);
        when(debugger.shouldStop()).thenReturn(false);
        RuntimeException runtimeException = new RuntimeException("Error running robot");
        when(workerRobotFactory.construct(any(), any())).thenThrow(runtimeException);

        WorkerThread workerThread = new WorkerThread(queue, control, false, workerRobotFactory);

        // run
        workerThread.run();

        // verify
        verify(debugger).handle(runtimeException);
        verify(control).signalStop();
    }

}
