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
package nl.xillio.xill.plugins.codec.hash.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.io.IOStream;
import nl.xillio.xill.api.io.SimpleIOStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.testng.Assert.assertEquals;

public class ToMD5ConstructTest extends TestUtils {
    private final ToMD5Construct construct = new ToMD5Construct();

    @Test
    public void testStringInput() {
        String inputString = "Dearest reader, I will now be pitched in to the depths from where I cannot return. I say farewell";
        MetaExpression input = fromValue(inputString);

        MetaExpression result = process(construct, input);

        assertEquals(result.getStringValue(), "898bc4d930a42e95741c8e747c5c8d5c");
    }

    @Test
    public void testStreamInput() {
        InputStream inputStream = IOUtils.toInputStream("I'll see you on the other side");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MetaExpression input = fromValue(new SimpleIOStream(inputStream, getClass().getSimpleName()));
        MetaExpression output = fromValue(new SimpleIOStream(outputStream, getClass().getSimpleName()));

        MetaExpression result = process(construct, input, output);

        assertEquals(result.getStringValue(), "00fbf1126c5b3008d38a0e3c170c3a32");
        assertEquals(outputStream.toString(), "I'll see you on the other side");
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testOutputNotStream() {
        process(construct, fromValue("Hello World"), fromValue("I am not a stream"));
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testNullInput() {
        process(construct, NULL);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testIOException() {
        IOStream ioStream = new ErrorIOStream();
        MetaExpression input = fromValue(ioStream);
        process(construct, input);
    }

    public static class ErrorIOStream implements IOStream {
        @Override
        public boolean hasInputStream() {
            return true;
        }

        @Override
        public BufferedInputStream getInputStream() throws IOException {
            throw new IOException("THIS IS AN ERROR STREAM");
        }

        @Override
        public boolean hasOutputStream() {
            return true;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("THIS IS AN ERROR STREAM");
        }

        @Override
        public void close() {

        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}