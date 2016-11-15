package nl.xillio.xill.webservice.model;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.OutputHandler;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.XillProcessor;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Robot;
import nl.xillio.xill.api.errors.XillParsingException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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
    private XillProcessor xillProcessor;
    private Robot robot;
    private Debugger debugger;

    private Path workingDir;
    private Path robotPath;

    private XillRuntimeImpl xillRuntime;

    @BeforeMethod
    public void mockEnvironment () throws IOException {
        xillEnvironment = mock(XillEnvironment.class);
        xillProcessor = mock(XillProcessor.class);
        robot = mock(Robot.class);
        debugger = mock(Debugger.class);
        when(xillEnvironment.buildProcessor(any(), any())).thenReturn(xillProcessor);
        when(xillProcessor.getRobot()).thenReturn(robot);
        when(xillProcessor.getDebugger()).thenReturn(debugger);

        xillRuntime = new XillRuntimeImpl();
        xillRuntime.setXillEnvironmentProvider(() -> xillEnvironment);

        workingDir = Paths.get("/path/to/working/dir");
        robotPath = Paths.get("/path/to/robot.xill");
    }

    /**
     * Test {@link XillRuntimeImpl#compile(Path, Path)} under normal circumstances.
     */
    @Test
    public void testCompile() throws IOException, XillParsingException {
        // Run
        xillRuntime.compile(workingDir, robotPath);

        // Verify
        verify(xillEnvironment).buildProcessor(workingDir, robotPath);
        verify(xillProcessor).compile();
    }

    /**
     * Test {@link XillRuntimeImpl#compile(Path, Path)} when compilation fails.
     *
     * All possible checked exceptions should be converted to runtime exceptions to prevent
     * having too many exceptions in the signature and too many layers handling exceptions.
     */
    @Test(expectedExceptions = RuntimeException.class)
    public void testCompileError() throws IOException {
        // Mock
        when(xillEnvironment.buildProcessor(workingDir, robotPath)).thenThrow(IOException.class);

        // Run
        xillRuntime.compile(workingDir, robotPath);
    }

    /**
     * Test {@link XillRuntimeImpl#runRobot(Map, OutputHandler)} under normal circumstances.
     */
    @Test
    public void testRunRobot() throws IOException {
        // Mock
        Double resultNumber = 42.0;
        MetaExpression robotResult = parseObject(resultNumber);
        when(robot.process(debugger)).thenReturn(InstructionFlow.doReturn(robotResult));

        Map<String, Object> parameters = new HashMap<>();
        OutputHandler outputHandler = mock(OutputHandler.class);

        // Run
        xillRuntime.compile(workingDir, robotPath);
        Object result = xillRuntime.runRobot(parameters, outputHandler);

        // Verify
        verify(xillEnvironment).buildProcessor(workingDir, robotPath);
        verifyNoMoreInteractions(xillEnvironment);
        verify(robot).process(debugger);

        // Assert
        assertSame(result, resultNumber);
    }

    /**
     * Test {@link XillRuntimeImpl#runRobot(Map, OutputHandler)} when {@link XillRuntimeImpl#compile(Path, Path)} has
     * not been called yet.
     */
    @Test(expectedExceptions = Exception.class)
    public void testRunRobotNoCompile() {
        // Mock
        Map<String, Object> parameters = new HashMap<>();
        OutputHandler outputHandler = mock(OutputHandler.class);

        // Run
        xillRuntime.runRobot(parameters, outputHandler);
    }
}
