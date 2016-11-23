package nl.xillio.xill.webservice.xill;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.OutputHandler;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.XillProcessor;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Robot;
import nl.xillio.xill.api.errors.XillParsingException;
import nl.xillio.xill.webservice.exceptions.XillCompileException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

/**
 * Tests for {@link XillRuntimeImpl}.
 *
 * @author Geert Konijnendijk
 */
public class XillRuntimeImplTest extends TestUtils {

    private XillEnvironment xillEnvironment;
    private OutputHandler outputHandler;
    private ThreadPoolTaskExecutor compileExecutor;
    private XillProcessor xillProcessor;
    private Robot robot;
    private Debugger debugger;

    private Path workingDir;
    private Path robotPath;

    private XillRuntimeImpl xillRuntime;

    @BeforeMethod
    public void mockEnvironment () throws IOException {
        xillEnvironment = mock(XillEnvironment.class);
        outputHandler = mock(OutputHandler.class);
        compileExecutor = mock(ThreadPoolTaskExecutor.class);
        xillProcessor = mock(XillProcessor.class);
        robot = mock(Robot.class);
        debugger = mock(Debugger.class);
        when(xillEnvironment.buildProcessor(any(), any())).thenReturn(xillProcessor);
        when(xillProcessor.getRobot()).thenReturn(robot);
        when(xillProcessor.getDebugger()).thenReturn(debugger);

        xillRuntime = new XillRuntimeImpl(xillEnvironment, outputHandler, compileExecutor);

        workingDir = Paths.get("/path/to/working/dir");
        robotPath = Paths.get("robot.xill");
    }

    /**
     * Test {@link XillRuntimeImpl#compile(Path, Path)} under normal circumstances.
     */
    @Test
    public void testCompile() throws IOException, XillParsingException, XillCompileException {
        // Run
        xillRuntime.compile(workingDir, robotPath);

        // Verify
        verify(xillEnvironment).setXillThreadFactory(any());
        verify(xillEnvironment).buildProcessor(workingDir, workingDir.resolve(robotPath));
        verify(xillProcessor).compile();
    }

    /**
     * Test {@link XillRuntimeImpl#compile(Path, Path)} when compilation fails.
     *
     * All possible checked exceptions should be converted to runtime exceptions to prevent
     * having too many exceptions in the signature and too many layers handling exceptions.
     */
    @Test(expectedExceptions = XillCompileException.class)
    public void testCompileError() throws IOException, XillCompileException {
        // Mock
        when(xillEnvironment.buildProcessor(any(), any())).thenThrow(IOException.class);

        // Run
        xillRuntime.compile(workingDir, robotPath);
    }

    /**
     * Test {@link XillRuntimeImpl#runRobot(Map)} under normal circumstances.
     */
    @Test
    public void testRunRobot() throws IOException, XillCompileException, ExecutionException {
        // Mock
        Double resultNumber = 42.0;
        MetaExpression robotResult = parseObject(resultNumber);
        when(robot.process(debugger)).thenReturn(InstructionFlow.doReturn(robotResult));

        Map<String, Object> parameters = new HashMap<>();

        // Run
        xillRuntime.compile(workingDir, robotPath);
        Object result = xillRuntime.runRobot(parameters);

        // Verify
        verify(xillEnvironment).setXillThreadFactory(any());
        verify(xillEnvironment).buildProcessor(workingDir, workingDir.resolve(robotPath));
        verifyNoMoreInteractions(xillEnvironment);
        verify(robot).process(debugger);

        // Assert
        assertSame(result, resultNumber);
    }

    /**
     * Test {@link XillRuntimeImpl#runRobot(Map)} when {@link XillRuntimeImpl#compile(Path, Path)} has
     * not been called yet.
     */
    @Test
    public void testRunRobotNoCompile() throws ExecutionException {
        // Mock
        Map<String, Object> parameters = new HashMap<>();

        // Run
        Object result = xillRuntime.runRobot(parameters);

        // Assert
        assertSame(result, null);
    }
}
