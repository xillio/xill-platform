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
package nl.xillio.xill.plugins.codec.decode.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.io.IOStream;
import nl.xillio.xill.api.io.SimpleIOStream;
import nl.xillio.xill.plugins.codec.hash.constructs.ToMD5ConstructTest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.testng.Assert.assertEquals;


public class FromBase64ConstructTest extends TestUtils {
    private final FromBase64Construct construct = new FromBase64Construct();

    @Test
    public void testDecodeString() {
        String inputString = "SGVsbG8gV29ybGQsIEkgaGF2ZSBiZWVuIGRlY29kZWQgY29ycmVjdGx5";
        MetaExpression input = fromValue(inputString);

        MetaExpression result = process(construct, input);

        assertEquals(result.getStringValue(), "Hello World, I have been decoded correctly");
    }

    @Test
    public void testDecodeInputStream() throws IOException {
        InputStream inputStream = IOUtils.toInputStream("SXQgc2VlbXMgSSBoYXZlIGRlY29kZWQgdGhlIHN0cmVhbSBpbiBhIHJpZ2h0bHkgbWFuZXI=");
        IOStream ioStream = new SimpleIOStream(inputStream, getClass().getSimpleName());
        MetaExpression input = fromValue(ioStream);

        MetaExpression result = process(construct, input);

        String value = IOUtils.toString(result.getBinaryValue().getInputStream());
        assertEquals(value, "It seems I have decoded the stream in a rightly maner");
    }

    @Test
    public void testDecodeOutputStream() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOStream ioStream = new SimpleIOStream(outputStream, getClass().getSimpleName());
        MetaExpression input = fromValue(ioStream);

        MetaExpression result = process(construct, input);

        OutputStream base64Stream = result.getBinaryValue().getOutputStream();
        // Now writing to the output stream in base64 should result in decoded content in the original stream

        assertEquals(outputStream.toString(), "");

        IOUtils.write("VGhlIG5ld2x5IGNyZWF0ZWQgb3V0cHV0IHN0cmVhbSBzZWVtcyB0byBiZSBmdWxseSBvcGVyYXRpb25hbA==", base64Stream);

        assertEquals(outputStream.toString(), "The newly created output stream seems to be fully operational");
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testIOException() {
        process(construct, fromValue(new ToMD5ConstructTest.ErrorIOStream()));
    }
}