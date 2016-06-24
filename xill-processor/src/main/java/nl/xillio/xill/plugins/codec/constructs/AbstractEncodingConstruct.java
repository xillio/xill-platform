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
package nl.xillio.xill.plugins.codec.constructs;

import me.biesaart.utils.IOUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.io.ComposedIOStream;
import nl.xillio.xill.api.io.IOStream;
import nl.xillio.xill.plugins.stream.utils.StreamUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * This construct represents an abstract implementation of a construct that can encode a stream or string.
 *
 * @author Thomas Biesaart
 */
public abstract class AbstractEncodingConstruct extends Construct {
    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                this::tryProcess,
                new Argument("input", ATOMIC),
                new Argument("charset", fromValue("UTF-8"), ATOMIC)
        );
    }

    @SuppressWarnings("squid:UnusedPrivateMethod") // Sonar still doesn't support method references
    private MetaExpression tryProcess(MetaExpression input, MetaExpression charset) {
        try {
            return process(input, charset);
        } catch (IOException e) {
            throw new OperationFailedException("encode " + input, e.getMessage(), e);
        }
    }

    private MetaExpression process(MetaExpression input, MetaExpression charset) throws IOException {
        // Check for input and/or output steams
        BufferedInputStream inputStream = getInputStream(input);
        OutputStream outputStream = getOutputStream(input);

        // If we have no streams we will process this as a plain string
        if (inputStream == null && outputStream == null) {
            return processString(input, charset);
        }

        // Build the new streams
        InputStream encodedInputStream = inputStream == null ? null : encode(inputStream);
        OutputStream encodedOutputStream = outputStream == null ? null : encode(outputStream);

        IOStream ioStream = new ComposedIOStream(encodedInputStream, encodedOutputStream, null, input);
        return fromValue(ioStream);
    }

    protected abstract InputStream encode(BufferedInputStream inputStream);

    protected abstract OutputStream encode(OutputStream outputStream);

    private MetaExpression processString(MetaExpression input, MetaExpression encoding) throws IOException {
        Charset charset = StreamUtils.getCharset(encoding);

        try (InputStream stringStream = IOUtils.toInputStream(input.getStringValue());
             BufferedInputStream bufferedInputStream = new BufferedInputStream(stringStream);
             InputStream encodedStream = encode(bufferedInputStream)) {
            String value = IOUtils.toString(encodedStream, charset);
            return fromValue(value);
        }
    }

    private OutputStream getOutputStream(MetaExpression input) throws IOException {
        if (input.getBinaryValue().hasOutputStream()) {
            return input.getBinaryValue().getOutputStream();
        }
        return null;
    }

    private BufferedInputStream getInputStream(MetaExpression input) throws IOException {
        if (input.getBinaryValue().hasInputStream()) {
            return input.getBinaryValue().getInputStream();
        }
        return null;
    }
}
