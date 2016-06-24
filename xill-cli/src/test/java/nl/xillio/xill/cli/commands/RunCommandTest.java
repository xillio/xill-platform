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
package nl.xillio.xill.cli.commands;

import me.biesaart.wield.error.IllegalUserInputException;
import me.biesaart.wield.input.Arguments;
import me.biesaart.wield.input.Flag;
import me.biesaart.wield.input.Flags;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.errors.XillParsingException;
import nl.xillio.xill.cli.services.CompileException;
import nl.xillio.xill.cli.services.XillExecutionService;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class RunCommandTest {
    private Path tmpFile;

    @BeforeClass
    public void createFile() throws IOException {
        tmpFile = Files.createTempFile("unitTest", ".xill");
    }

    @AfterClass
    public void deleteFile() throws IOException {
        Files.deleteIfExists(tmpFile);
    }

    @Test(expectedExceptions = IllegalUserInputException.class)
    public void testRunNoArgs() throws Exception {
        RunCommand runCommand = new RunCommand(null, null);

        runCommand.run(new Arguments(), new Flags());
    }

    @Test
    public void testInitIfRequired() throws IOException, XillParsingException {
        XillExecutionService xillExecutionService = mock(XillExecutionService.class);
        when(xillExecutionService.run(any(), any())).thenReturn(new ArrayList<>());
        InitCommand initCommand = mock(InitCommand.class);
        RunCommand runCommand = new RunCommand(xillExecutionService, initCommand);

        Arguments args = new Arguments();
        args.add(tmpFile.toAbsolutePath().toString());


        runCommand.run(args, new Flags());

        // Test if the init command is invoked
        verify(initCommand).run(any());
    }

    @Test
    public void testSyntaxExceptionCaught() throws IOException, XillParsingException {
        testException(new CompileException(new XillParsingException("Hello World", 2, RobotID.dummyRobot())));
    }

    @Test
    public void testIOExceptionCaught() throws IOException, XillParsingException {
        testException(new IOException("Hey"));
    }

    private void testException(Exception e) throws IOException, XillParsingException {
        XillExecutionService xillExecutionService = mock(XillExecutionService.class);
        when(xillExecutionService.run(any(), any())).thenThrow(e);
        InitCommand initCommand = mock(InitCommand.class);
        RunCommand runCommand = new RunCommand(xillExecutionService, initCommand);

        Arguments args = new Arguments();
        args.add(tmpFile.toAbsolutePath().toString());

        runCommand.run(args, new Flags());

        // If we reach this then the exception is caught
        verify(xillExecutionService).run(any(), any());
    }

    @Test(expectedExceptions = IllegalUserInputException.class)
    public void testRunRobotNotFound() throws IOException {
        XillExecutionService xillExecutionService = mock(XillExecutionService.class);
        InitCommand initCommand = mock(InitCommand.class);
        RunCommand runCommand = new RunCommand(xillExecutionService, initCommand);


        Arguments args = new Arguments();
        args.add("I Do Not Exist");

        Flags flags = new Flags();
        flags.add(new Flag("p", "."));

        runCommand.run(args, flags);

    }


}
