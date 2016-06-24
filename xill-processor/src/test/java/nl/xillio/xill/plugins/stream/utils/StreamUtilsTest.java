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
package nl.xillio.xill.plugins.stream.utils;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.io.IOStream;
import nl.xillio.xill.api.io.SimpleIOStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;


public class StreamUtilsTest extends TestUtils {

    @Test
    public void testGetInputStream() throws Exception {
        InputStream stream = IOUtils.toInputStream("Hey");
        MetaExpression value = fromValue(new SimpleIOStream(stream, null));
        InputStream result = StreamUtils.getInputStream(value, "");
        assertEquals(IOUtils.toString(result), "Hey");
        stream.close();
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*myInput.*")
    public void testGetInputStreamNoBinary() {
        StreamUtils.getInputStream(fromValue("Hello"), "myInput");
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testGetInputStreamIOException() throws IOException {
        IOStream ioStream = spy(new SimpleIOStream(null, null, null));
        doReturn(true).when(ioStream).hasInputStream();
        doThrow(IOException.class).when(ioStream).getInputStream();

        StreamUtils.getInputStream(fromValue(ioStream), "");
    }

    @Test
    public void testGetOutputStream() throws Exception {
        OutputStream stream = new ByteArrayOutputStream();
        MetaExpression value = fromValue(new SimpleIOStream(stream, null));
        OutputStream result = StreamUtils.getOutputStream(value, "");
        assertSame(result, stream);
        stream.close();
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*myInput.*")
    public void testGetOutputStreamNoBinary() {
        StreamUtils.getOutputStream(fromValue("Hello"), "myInput");
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testGetOutputStreamIOException() throws IOException {
        IOStream ioStream = spy(new SimpleIOStream(null, null, null));
        doReturn(true).when(ioStream).hasOutputStream();
        doThrow(IOException.class).when(ioStream).getOutputStream();

        StreamUtils.getOutputStream(fromValue(ioStream), "");
    }
}