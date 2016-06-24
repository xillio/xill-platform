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
package nl.xillio.xill.plugins.system.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.system.exec.ProcessDescription;
import nl.xillio.xill.plugins.system.exec.ProcessFactory;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test the {@link ExecConstruct}
 */
public class ExecConstructTest extends TestUtils {

    /**
     * Test invoke the construct with a string as command and default working directory
     */
    @Test
    public void testProcessString() throws InterruptedException {
        // Mock the context
        MetaExpression arguments = mockAtomicCommand();

        MetaExpression directory = NULL;
        ProcessFactory processFactory = mockProcessFactory(1, 2, 0);

        ArgumentCaptor<ProcessDescription> descriptionCaptor = ArgumentCaptor.forClass(ProcessDescription.class);

        // Run the method
        MetaExpression result = new ExecConstruct().process(arguments, directory, processFactory, context());

        // Make assertions
        verify(processFactory).apply(descriptionCaptor.capture());

        assertEquals(descriptionCaptor.getValue().getCommands(), new String[]{"TestCommand"});
        assertEquals(result.getType(), OBJECT);

        @SuppressWarnings("unchecked")
        Map<String, MetaExpression> value = (Map<String, MetaExpression>) result.getValue();

        assertEquals(value.get("output").toString(), "[\"\\u0000\"]");
        assertEquals(value.get("output").getType(), LIST);
        assertEquals(value.get("errors").toString(), "[\"\\u0000\\u0000\"]");
        assertEquals(value.get("errors").getType(), LIST);
        assertNotNull(value.get("runtime"));
        assertEquals(value.get("runtime").getType(), ATOMIC);
        assertEquals(value.get("exitCode").getNumberValue(), 0);
        assertEquals(value.get("exitCode").getType(), ATOMIC);
    }

    /**
     * Test invoke the construct with a list as command and default working directory
     */
    @Test
    public void testProcessList() throws InterruptedException {
        // Mock the context
        MetaExpression arguments = mockExpression(LIST);
        when(arguments.getValue()).thenReturn(Arrays.asList(
                fromValue("Test"),
                fromValue("command"),
                fromValue("-t")));

        MetaExpression directory = NULL;
        ProcessFactory processFactory = mockProcessFactory(6, 3, 0);

        ArgumentCaptor<ProcessDescription> descriptionCaptor = ArgumentCaptor.forClass(ProcessDescription.class);

        // Run the method
        MetaExpression result = new ExecConstruct().process(arguments, directory, processFactory, context());

        // Make assertions
        verify(processFactory).apply(descriptionCaptor.capture());

        assertEquals(descriptionCaptor.getValue().getCommands(), new String[]{"Test", "command", "-t"});

        assertEquals(result.getType(), OBJECT);

        @SuppressWarnings("unchecked")
        Map<String, MetaExpression> value = (Map<String, MetaExpression>) result.getValue();

        assertEquals(value.get("output").toString(), "[\"\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\"]");
        assertEquals(value.get("output").getType(), LIST);
        assertEquals(value.get("errors").toString(), "[\"\\u0000\\u0000\\u0000\"]");
        assertEquals(value.get("errors").getType(), LIST);
        assertNotNull(value.get("runtime"));
        assertEquals(value.get("runtime").getType(), ATOMIC);
        assertEquals(value.get("exitCode").getNumberValue(), 0);
        assertEquals(value.get("exitCode").getType(), ATOMIC);
    }

    /**
     * Make an input stream that will return a certain amount of null characters
     *
     * @param length
     * @return
     */
    private static InputStream mockStream(final int length) {
        InputStream stream = mock(InputStream.class);

        try {
            when(stream.read(any(), anyInt(), anyInt())).thenReturn(length, -1);

            when(stream.available()).thenReturn(length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream;
    }

    /**
     * Mock a {@link ProcessFactory} that will output a given number of null characters on the output and error stream
     *
     * @param outputStreamLength Number of null characters the output stream returns
     * @param errorStreamLength  Number of null characters the error stream returns
     * @param exitCode           The exit code the process should have
     * @return The mocked {@link ProcessFactory}
     */
    private ProcessFactory mockProcessFactory(int outputStreamLength, int errorStreamLength, int exitCode) throws InterruptedException {
        ProcessFactory processFactory = mock(ProcessFactory.class);
        Process process = mock(Process.class);
        InputStream out = mockStream(outputStreamLength);
        InputStream err = mockStream(errorStreamLength);
        when(process.getInputStream()).thenReturn(out);
        when(process.getErrorStream()).thenReturn(err);
        when(process.waitFor()).thenReturn(exitCode);
        when(processFactory.apply(any())).thenReturn(process);
        return processFactory;
    }

    /**
     * @return A single atomic argument containing a string value
     */
    private MetaExpression mockAtomicCommand() {
        MetaExpression arguments = mockExpression(ATOMIC);
        when(arguments.getStringValue()).thenReturn("TestCommand");
        return arguments;
    }

}
