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


import me.biesaart.utils.IOUtils;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

public class StreamUtils {
    private StreamUtils() {
        // No one shall have an instance
    }

    /**
     * This method is a wrapper that allows you to get a stream from an expression.
     *
     * @param expression    the expression
     * @param parameterName the parameter name
     * @return the stream
     * @throws RobotRuntimeException if the provided expression does not have an input stream
     * @throws OperationFailedException if the provided stream cannot be opened
     */
    public static BufferedInputStream getInputStream(MetaExpression expression, String parameterName) {
        if (!expression.getBinaryValue().hasInputStream()) {
            throw new RobotRuntimeException("Expected a reading data stream for parameter " + parameterName + " but found: " + expression);
        }

        try {
            return expression.getBinaryValue().getInputStream();
        } catch (IOException e) {
            throw new OperationFailedException("open stream",e.getMessage(), e);
        }
    }

    /**
     * This method is a wrapper that allows you to get a stream from an expression.
     *
     * @param expression    the expression
     * @param parameterName the parameter name
     * @return the stream
     * @throws RobotRuntimeException if the provided expression does not have an output stream
     * @throws OperationFailedException if the provided stream cannot be opened
     */
    public static OutputStream getOutputStream(MetaExpression expression, String parameterName) {
        if (!expression.getBinaryValue().hasOutputStream()) {
            throw new RobotRuntimeException("Expected a reading data stream for parameter " + parameterName + " but found: " + expression);
        }

        try {
            return expression.getBinaryValue().getOutputStream();
        } catch (IOException e) {
            throw new OperationFailedException("open stream",e.getMessage(), e);
        }
    }

    /**
     * This method will read a single line from a buffered input stream.
     * The end of a line is defined as <code>\n</code>, <code>\r</code>, <code>\r\n</code> or <code>EOF</code>.
     *
     * @param inputStream the input stream
     * @param charset     the character encoding
     * @return the line or null if the
     * @throws IOException  Is thrown if a stream(-related) operation fails.
     */
    public static String readLine(BufferedInputStream inputStream, Charset charset) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            return readLine(inputStream,charset,outputStream);
        }
    }

    private static String readLine(BufferedInputStream inputStream, Charset charset,ByteArrayOutputStream outputStream) throws IOException{
        int c = inputStream.read();
        if (c == -1) {
            throw new IOException("The end of the stream has been reached");
        }

        while (c != '\n' && c != '\r' && c != -1) {
            outputStream.write(c);
            c = inputStream.read();
        }

        if (c == '\r') {
            // The last character was a return character, if this
            // is followed by the \n character then that is part of the newline

            inputStream.mark(1);
            c = inputStream.read();

            if (c != '\n') {
                // That character was not a newline, put it back
                inputStream.reset();
            }
        }

        return new String(outputStream.toByteArray(), charset);
    }

    /**
     * This will construct an output stream that will forward all its data to an array of delegates.
     * This stream will also register a reference to the passed expression that will be released when the stream is closed.
     *
     * @param hostingExpression the expression for which to create the forking stream.
     *                          This must be a {@link nl.xillio.xill.api.components.ExpressionDataType#LIST} of output streams
     * @return the newly created output stream
     * @throws IOException              when an IO error occurs
     * @throws RobotRuntimeException    if the provided expression contains an element that is not an output stream
     * @throws IllegalArgumentException if the provided expression is not a list
     */
    public static OutputStream fork(MetaExpression hostingExpression) throws IOException {
        if (hostingExpression.getType() != ExpressionDataType.LIST) {
            throw new IllegalArgumentException("Provided expression must be a list");
        }

        OutputStream[] streams = getStreams(hostingExpression.getValue());
        return new ForkingOutputStream(hostingExpression, streams);
    }

    private static OutputStream[] getStreams(List<MetaExpression> outputsValue) throws IOException {
        OutputStream[] result = new OutputStream[outputsValue.size()];

        int i = 0;
        for (MetaExpression expression : outputsValue) {
            if (!expression.getBinaryValue().hasOutputStream()) {
                throw new RobotRuntimeException(expression + " does not contain binary data");
            }
            result[i++] = expression.getBinaryValue().getOutputStream();
        }

        return result;
    }

    /**
     * Get a {@link Charset} from a {@link MetaExpression}.
     * This will attempt to get a charset by name.
     *
     * @param encoding the expression from which to get a charset
     * @return the charset
     * @throws RobotRuntimeException if the charset is not found or supported
     */
    public static Charset getCharset(MetaExpression encoding) {
        if (encoding.isNull()) {
            return Charset.defaultCharset();
        }

        try {
            return Charset.forName(encoding.getStringValue());
        } catch (IllegalArgumentException e) {
            throw new RobotRuntimeException("Encoding " + encoding.getStringValue() + " is not supported", e);
        }
    }

    /**
     * Read text from an input stream.
     *
     * @param inputStream the stream
     * @param charset     the encoding that should be used
     * @param limit       the maximum amount of bytes that should be read
     * @return the string
     * @throws IOException  Is thrown if a stream(-related) operation fails.
     */
    public static String readText(BufferedInputStream inputStream, Charset charset, long limit) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            IOUtils.copyLarge(inputStream, outputStream, 0, limit);
            return new String(outputStream.toByteArray(), charset);
        }
    }
}
