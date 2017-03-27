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
package n.xillio.xill.cli;

import nl.xillio.xill.XillProcessor;
import org.apache.commons.io.FileUtils;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class XillCLITest {

    @Test
    public void testCorrectUsage() throws RobotExecutionException {
        XillRobotExecutor executor = mock(XillRobotExecutor.class);
        XillCLI xillCLI = new XillCLI();
        xillCLI.setXillRobotExecutor(executor);
        xillCLI.setArgs(new String[]{"MyRobot"});
        ProgramReturnCode returnCode = xillCLI.run();

        assertEquals(returnCode, ProgramReturnCode.OK);

        ArgumentCaptor<String> robotCaptor = ArgumentCaptor.forClass(String.class);
        verify(executor).execute(robotCaptor.capture());
        assertEquals(robotCaptor.getValue(), "MyRobot");
    }

    @Test
    public void testPrintVersion() {
        XillCLI xillCLI = new XillCLI();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        xillCLI.setStdOut(printStream);
        xillCLI.setArgs(new String[]{"-v"});

        xillCLI.run();


        // Validate the results
        printStream.flush();
        String text = outputStream.toString();

        // Contains Java Version
        assertTrue(
                text.contains(System.getProperty("java.version"))
        );

        // Contains Xill Version
        if (XillProcessor.class.getPackage().getImplementationVersion() != null) {
            // We only check this is version information is available. (When application is packaged)
            assertTrue(
                    text.contains(XillProcessor.class.getPackage().getImplementationVersion())
            );
        }
    }

    @Test
    public void testHelpVersion() {
        XillCLI xillCLI = new XillCLI();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        xillCLI.setStdOut(printStream);
        xillCLI.setArgs(new String[]{"-h"});

        xillCLI.run();


        // Validate the results
        printStream.flush();
        String text = outputStream.toString();

        // Contains Usage information
        assertTrue(
                text.contains("Usage")
        );

        // Contains Information about streams
        assertTrue(
                text.contains("argument std;")
        );
    }

    @Test
    public void testExecutionExceptionResult() throws RobotExecutionException {
        XillRobotExecutor executor = mock(XillRobotExecutor.class);
        doThrow(new RobotExecutionException("This is a unit test")).when(executor).execute(anyString());

        XillCLI xillCLI = new XillCLI();
        xillCLI.setXillRobotExecutor(executor);
        xillCLI.setArgs(new String[]{"MyRobot"});
        ProgramReturnCode returnCode = xillCLI.run();

        assertEquals(returnCode, ProgramReturnCode.EXECUTION_ERROR);
    }

    @Test
    public void testErrorOnInvalidOption() throws RobotExecutionException {

        XillCLI xillCLI = new XillCLI();
        xillCLI.setArgs(new String[]{"-t"});
        xillCLI.setStdErr(new PrintStream(new ByteArrayOutputStream()));
        ProgramReturnCode returnCode = xillCLI.run();

        assertEquals(returnCode, ProgramReturnCode.INVALID_INPUT);
    }

    @Test
    public void testRunActualRobot() throws IOException {
        // Create a folder with a robot
        Path projectDir = Files.createTempDirectory("xill-cli-test");
        Path robotFile = projectDir.resolve("fqn/Path.xill");
        Files.createDirectories(robotFile.getParent());
        Files.write(robotFile, Collections.singletonList("use System; System.print('Hello World. This is a test!');"), StandardOpenOption.CREATE);


        // Run by path
        XillCLI xillCLI = new XillCLI();
        xillCLI.setArgs(new String[]{"fqn.Path", "-w", projectDir.toString()});

        // Validate
        ProgramReturnCode returnCode = xillCLI.run();
        assertEquals(returnCode, ProgramReturnCode.OK);

        // Delete project
        Files.delete(robotFile);
        Files.delete(robotFile.getParent());
        Files.delete(projectDir);

        // Run again (file does not exist)
        returnCode = xillCLI.run();
        assertEquals(returnCode, ProgramReturnCode.EXECUTION_ERROR);
    }

    @Test
    public void testRunRobotWithIncludes() throws IOException {
        // Create a folder with a robot.
        Path tempDir = Files.createDirectories(Paths.get("target/xill-cli-test"));
        Path projectDir = tempDir.resolve("project");
        Path includedDir = tempDir.resolve("includedDir");
        Files.createDirectories(projectDir);
        Files.createDirectories(includedDir);


        // Create an archive with a robot.
        Path includedArchive = tempDir.resolve("archive.xip");
        ZipOutputStream archiveStream = new ZipOutputStream(new FileOutputStream(includedArchive.toFile()));
        ZipEntry entry = new ZipEntry("ArchiveRobot.xill");
        archiveStream.putNextEntry(entry);
        archiveStream.write("use System; function archive() { System.print('This is an included robot from an archive.'); }".getBytes());
        archiveStream.closeEntry();
        archiveStream.close();

        // The project robots.
        Path baseRobot = projectDir.resolve("BaseRobot.xill");
        Files.write(baseRobot, Collections.singletonList(
                "use System;" +
                        "include ProjectRobot; include DirectoryRobot; include ArchiveRobot;" +
                        "System.print('This is the base robot.');" +
                        "project(); directory(); archive();"
        ), StandardOpenOption.CREATE);
        Path projectRobot = projectDir.resolve("ProjectRobot.xill");
        Files.write(projectRobot, Collections.singletonList("use System; function project() { System.print('This is another project robot.'); }"), StandardOpenOption.CREATE);

        // The included directory robot.
        Path directoryRobot = includedDir.resolve("DirectoryRobot.xill");
        Files.write(directoryRobot, Collections.singletonList("use System; function directory() { System.print('This is an included robot from a directory.'); }"), StandardOpenOption.CREATE);

        // Run the robot.
        XillCLI xillCLI = new XillCLI();
        xillCLI.setArgs(new String[]{"BaseRobot", "-w", projectDir.toString(), "-r", includedDir + ":" + includedArchive});
        ProgramReturnCode returnCode = xillCLI.run();
        assertEquals(returnCode, ProgramReturnCode.OK);

        // Delete project
        FileUtils.forceDeleteOnExit(tempDir.toFile());
    }
}
