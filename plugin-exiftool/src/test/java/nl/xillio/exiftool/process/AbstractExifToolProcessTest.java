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
package nl.xillio.exiftool.process;

import org.apache.commons.io.IOUtils;
import org.mockito.stubbing.Stubber;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertTrue;


public class AbstractExifToolProcessTest {

    public static final String EXIFTOOL_PATH = "/usr/bin/exiftool";

    @Test
    public void testClose() throws IOException {
        MockProcess process = new MockProcess(IOUtils.toInputStream(""));
        process.start();
        process.close();

        // Test if the process is killed.
        verify(process.buildProcess(null)).destroyForcibly();
        assertTrue(process.isClosed());
        assertFalse(process.isAvailable());
        assertFalse(process.isRunning());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCantStartTwice() throws IOException {
        MockProcess process = new MockProcess(IOUtils.toInputStream(""));
        process.start();
        process.start();
    }

    @Test
    public void testRun() throws IOException {
        MockProcess process = new MockProcess(IOUtils.toInputStream("Line A\nLine B"));

        ExecutionResult result = process.run("Some", "Input");

        assertEquals(result.next(), "Line A");
        assertEquals(result.next(), "Line B");
        Assert.assertFalse(result.hasNext());
    }

    /**
     * Test {@link AbstractExifToolProcess#searchExiftoolOnPath()} when exiftool is present on the PATH
     */
    @Test
    public void testSearchExiftoolOnPathPresent() throws IOException {
        Path exiftoolPath = mock(Path.class, RETURNS_DEEP_STUBS);
        when(exiftoolPath.toAbsolutePath().toString()).thenReturn(EXIFTOOL_PATH);
        MockProcess process = spyMockProcess(new String[]{"/usr/local/bin", "/usr/bin"}, false, Optional.empty(), Optional.of(exiftoolPath));

        String result = process.searchExiftoolOnPath();

        verify(process).getPathEntries();
        verify(process, times(2)).findExiftool(anyString());

        assertSame(result, EXIFTOOL_PATH, "Returned exiftool path did not match found path");
    }

    /**
     * Test {@link AbstractExifToolProcess#searchExiftoolOnPath()} when exiftool is not present on the PATH
     */
    @Test
    public void testSearchExiftoolOnPathNotPresent() throws IOException {
        MockProcess process = spyMockProcess(new String[]{"/usr/local/bin", "/usr/bin"}, false, Optional.empty(), Optional.empty());

        String result = process.searchExiftoolOnPath();

        verify(process).getPathEntries();
        verify(process, times(2)).findExiftool(anyString());

        assertNull(result);
    }

    /**
     * Test that {@link AbstractExifToolProcess#searchExiftoolOnPath()} does not throw an exception when searching exiftool fails
     */
    @Test
    public void testSearchExiftoolOnPathInvalidPath() throws IOException {
        MockProcess process = spyMockProcess(new String[]{"/usr/local/bin", "/usr/bin"}, true);

        String result = process.searchExiftoolOnPath();

        verify(process).getPathEntries();
        verify(process, times(2)).findExiftool(anyString());

        assertNull(result);
    }

    /**
     * Create a spied {@link MockProcess} with the methods {@link MockProcess#getPathEntries()}
     * and {@link MockProcess#findExiftool(String)} spied and returning the parameters passed
     * to this method.
     *
     * @param pathEntries         The return value of {@link MockProcess#getPathEntries()}
     * @param exceptionInFind When true, an exception will be thrown when calling {@link MockProcess#findExiftool(String)}
     * @param exiftoolExecutables Each parameter is a return value of {@link MockProcess#findExiftool(String)},
     *                            should be the same length as pathEntries
     * @return The spied {@link MockProcess}
     */
    private MockProcess spyMockProcess(String[] pathEntries, boolean exceptionInFind, Optional<Path>... exiftoolExecutables) throws IOException {
        MockProcess process = spy(new MockProcess(IOUtils.toInputStream("")));

        doReturn(pathEntries).when(process).getPathEntries();

        if (exceptionInFind) {
            doThrow(IOException.class).when(process).findExiftool(anyString());
        }
        else {
            Stubber returnValues = null;
            for (Optional<Path> returnValue : exiftoolExecutables) {
                if (returnValues == null) {
                    returnValues = doReturn(returnValue);
                } else {
                    returnValues = returnValues.doReturn(returnValue);
                }
            }
            returnValues.when(process).findExiftool(anyString());
        }

        return process;
    }

    private static class MockProcess extends AbstractExifToolProcess {
        private final Process process;

        private MockProcess(InputStream inputStream) {
            this.process = mock(Process.class, RETURNS_DEEP_STUBS);
            when(process.getInputStream()).thenReturn(inputStream);
        }

        @Override
        protected String getPathVariableSeparator() {
            return null;
        }

        @Override
        protected Process buildProcess(ProcessBuilder processBuilder) throws IOException {
            return process;
        }

        @Override
        public boolean needInit() {
            return false;
        }

        @Override
        public void init() throws IOException {

        }
    }
}
