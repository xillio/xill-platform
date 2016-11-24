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
package nl.xillio.xill.webservice.xill;

import me.biesaart.utils.IOUtils;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.OutputHandler;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.XillProcessor;
import nl.xillio.xill.api.components.*;
import nl.xillio.xill.api.errors.XillParsingException;
import nl.xillio.xill.api.io.IOStream;
import nl.xillio.xill.debugging.XillDebugger;
import nl.xillio.xill.plugins.stream.utils.StreamUtils;
import nl.xillio.xill.webservice.exceptions.XillCompileException;
import nl.xillio.xill.webservice.exceptions.XillInvalidStateException;
import nl.xillio.xill.webservice.xill.XillRuntimeImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.util.io.IOUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
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
     * Mock a sample result for a robot
     *
     * @param result The result the robot should return
     */
    private void mockRobotResult(Object result) {
        MetaExpression robotResult = parseObject(result);
        when(robot.process(debugger)).thenReturn(InstructionFlow.doReturn(robotResult));
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
    public void testRunRobot() throws IOException, XillCompileException {
        // Mock
        int resultNumber = 42;
        mockRobotResult(resultNumber);

        Map<String, Object> parameters = new HashMap<>();

        // Run
        xillRuntime.compile(workingDir, robotPath);
        Object result = xillRuntime.runRobot(parameters);

        // Verify
        verifyRobotRun();

        // Assert
        assertSame(result, resultNumber);
    }

    /**
     * Test {@link XillRuntimeImpl#runRobot(Map)} when the robot results in a stream.
     */
    @Test
    public void testRunRobotStreamResult() throws Exception {
        // Mock
        IOStream ioStream = mock(IOStream.class);
        BufferedInputStream inputStream = mock(BufferedInputStream.class);
        when(ioStream.hasInputStream()).thenReturn(true);
        when(ioStream.getInputStream()).thenReturn(inputStream);
        MetaExpression robotResult = mockExpression(ExpressionDataType.ATOMIC);
        when(robotResult.getBinaryValue()).thenReturn(ioStream);
        when(robot.process(debugger)).thenReturn(InstructionFlow.doReturn(robotResult));

        Map<String, Object> parameters = new HashMap<>();

        // Run
        xillRuntime.compile(workingDir, robotPath);
        Object result = xillRuntime.runRobot(parameters);

        // Verify
        verifyRobotRun();

        // Assert
        assertSame(result, inputStream);
    }

    /**
     * Test {@link XillRuntimeImpl#runRobot(Map)} when it gets a stream as input.
     */
    @Test
    public void testRunRobotStreamInput() throws Exception {
        // Mock
        mockRobotResult(42);

        String streamContents = "TestInputStream";
        InputStream inputStream = IOUtils.toInputStream(streamContents);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("stream", inputStream);

        // Run
        xillRuntime.compile(workingDir, robotPath);
        xillRuntime.runRobot(parameters);

        // Verify
        ArgumentCaptor<MetaExpression> argumentCaptor = ArgumentCaptor.forClass(MetaExpression.class);
        verify(robot).setArgument(argumentCaptor.capture());
        verifyRobotRun();

        // Assert
        Map<String, MetaExpression> receivedArgument = argumentCaptor.getValue().<Map<String, MetaExpression>>getValue();
        InputStream receivedStream = receivedArgument.get("stream").getBinaryValue().getInputStream();
        assertEquals(IOUtils.toString(receivedStream), streamContents, "The received stream did not contain the correct contents");
    }

    /**
     * Do all verifies needed after running a robot
     */
    private void verifyRobotRun() throws IOException {
        verify(xillEnvironment).setXillThreadFactory(any());
        verify(xillEnvironment).buildProcessor(workingDir, workingDir.resolve(robotPath));
        verifyNoMoreInteractions(xillEnvironment);
        verify(robot).process(debugger);
    }

    /**
     * Test {@link XillRuntimeImpl#runRobot(Map)} when {@link XillRuntimeImpl#compile(Path, Path)} has
     * not been called yet.
     */
    @Test
    public void testRunRobotNoCompile() {
        // Mock
        mockRobotResult(42);

        Map<String, Object> parameters = new HashMap<>();

        // Run
        Object result = xillRuntime.runRobot(parameters);

        // Assert
        assertSame(result, null);
    }



    /**
     * Test {@link XillRuntimeImpl#runRobot(Map)} when the asynchronous compile was interrupted.
     */
    @Test
    public void testRunRobotCompileInterrupted() throws Exception {
        testRunRobotCompileException(InterruptedException.class);
    }

    /**
     * Test {@link XillRuntimeImpl#runRobot(Map)} when the asynchronous compile caused an exception.
     */
    @Test
    public void testRunRobotCompileFailed() throws Exception {
        testRunRobotCompileException(ExecutionException.class);
    }

    /**
     * Test that running a robot returns {@code null} when the asynchronous compile fails with
     * a given exception.
     *
     * @param compileException The exception the asynchronous compile fails with
     */
    private void testRunRobotCompileException(Class<? extends Exception> compileException) throws Exception {
        // Mock
        mockRobotResult(42);

        Future<Object> compileSuccess = mock(Future.class);
        when(compileSuccess.get()).thenThrow(compileException);
        when(compileExecutor.submit(any(Callable.class))).thenReturn(compileSuccess);

        HashMap<String, Object> parameters = new HashMap<>();

        // Run
        xillRuntime.compile(workingDir, robotPath);
        // Run once to start the asynchronous compile
        xillRuntime.runRobot(parameters);
        // Run again to get the exception
        Object result = xillRuntime.runRobot(parameters);

        // Assert

        // When compilation fails, the runtime should return null
        assertEquals(result, null, "The runtime did not return null after compilation failed");
    }

    /**
     * Test if {@link XillDebugger#stop()} is called when aborting a robot
     */
    @Test
    public void testAbortRobot() throws XillCompileException {
        // Run
        xillRuntime.compile(workingDir, robotPath);
        xillRuntime.abortRobot();

        // verify
        verify(debugger).stop();
    }

    /**
     * Test if {@link XillRuntimeImpl#destroy()} properly closes resources.
     */
    @Test
    public void testDestroy() {
        // Run
        xillRuntime.destroy();

        // Verify
        verify(xillEnvironment).close();

        // The compile executor is shared, so should not be closed
        verifyZeroInteractions(compileExecutor);
    }
}
