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

import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.mockito.Mockito.*;

/**
 * Unit test for closing IO streams.
 *
 * @author Pieter Dirk Soels
 */
public class IOStreamTest {

    @Test
    public void testCloseNormalConditions() throws IOException {
        // Mock dependencies
        BufferedReader input = mock(BufferedReader.class);
        OutputStreamWriter output = mock(OutputStreamWriter.class);
        IOStream stream = new IOStream(input, output);

        // Run
        stream.close();

        // Verify
        verify(input, times(1)).close();
        verify(output, times(1)).close();
    }

    @Test
    public void testCloseInputException() throws IOException {
        // Mock dependencies
        BufferedReader input = mock(BufferedReader.class);
        OutputStreamWriter output = mock(OutputStreamWriter.class);
        IOStream stream = new IOStream(input, output);

        doThrow(IOException.class).when(input).close();

        // Run
        stream.close();

        // Verify
        verify(input, times(1)).close();
        verify(output, times(1)).close();
    }

    @Test
    public void testCloseOutputException() throws IOException {
        // Mock dependencies
        BufferedReader input = mock(BufferedReader.class);
        OutputStreamWriter output = mock(OutputStreamWriter.class);
        IOStream stream = new IOStream(input, output);

        doThrow(IOException.class).when(output).close();

        // Run
        stream.close();

        // Verify
        verify(input, times(1)).close();
        verify(output, times(1)).close();
    }
}
