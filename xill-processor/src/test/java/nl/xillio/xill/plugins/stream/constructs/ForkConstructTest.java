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
package nl.xillio.xill.plugins.stream.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.io.IOStream;
import nl.xillio.xill.api.io.SimpleIOStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;


public class ForkConstructTest extends TestUtils {
    private final ForkConstruct construct = new ForkConstruct();

    @Test
    public void testTwoStreamsNormalUsage() throws IOException {
        // Set up input
        OutputStream target1 = new ByteArrayOutputStream();
        OutputStream target2 = new ByteArrayOutputStream();

        IOStream output1 = new SimpleIOStream(target1, null);
        IOStream output2 = new SimpleIOStream(target2, null);

        // Process
        MetaExpression fork = ConstructProcessor.process(
                construct.prepareProcess(context(construct)),
                fromValue(Arrays.asList(fromValue(output1), fromValue(output2)))
        );

        // Write some data
        OutputStream forkStream = fork.getBinaryValue().getOutputStream();
        IOUtils.write("Hello World", forkStream);

        // Check the data
        assertEquals(target1.toString(), "Hello World");
        assertEquals(target2.toString(), "Hello World");
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*two.*")
    public void testEmptyListInput() {
        ConstructProcessor.process(
                construct.prepareProcess(context(construct)),
                emptyList()
        );
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*Hello World.*")
    public void nonBinaryInput() {
        ConstructProcessor.process(
                construct.prepareProcess(context(construct)),
                fromValue(Arrays.asList(fromValue("Hello World"), fromValue("Hello World")))
        );
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*create fork.*")
    public void testIOExceptionOnGetStream() throws IOException {
        IOStream ioStream = mock(IOStream.class);
        when(ioStream.hasOutputStream()).thenReturn(true);
        when(ioStream.getOutputStream()).thenThrow(IOException.class);
        MetaExpression value = fromValue(ioStream);

        ConstructProcessor.process(
                construct.prepareProcess(context(construct)),
                fromValue(Arrays.asList(value, fromValue("Hello World")))
        );

    }


}