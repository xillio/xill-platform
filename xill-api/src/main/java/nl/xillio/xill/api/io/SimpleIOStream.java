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
package nl.xillio.xill.api.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class is a simple implementation of {@link IOStream} based on two providers.
 *
 * @author Thomas biesaart
 */
public class SimpleIOStream extends AbstractIOStream {
    private final BufferedInputStream inputStream;
    private final OutputStream outputStream;

    public SimpleIOStream(InputStream stream, String description) {
        this(stream, null, description);
    }

    public SimpleIOStream(OutputStream stream, String description) {
        this(null, stream, description);
    }

    public SimpleIOStream(InputStream inputStream, OutputStream outputStream, String description) {
        super(description);
        this.inputStream = toBufferedStream(inputStream);
        this.outputStream = outputStream;
    }

    private BufferedInputStream toBufferedStream(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }

        if (inputStream instanceof BufferedInputStream) {
            return (BufferedInputStream) inputStream;
        }
        return new BufferedInputStream(inputStream);
    }

    @Override
    public boolean hasInputStream() {
        return inputStream != null;
    }

    @Override
    public BufferedInputStream getInputStream() throws IOException {
        if (!hasInputStream()) {
            throw new NoStreamAvailableException("No stream is available");
        }
        return inputStream;
    }

    @Override
    public boolean hasOutputStream() {
        return outputStream != null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (!hasOutputStream()) {
            throw new NoStreamAvailableException("No stream is available");
        }
        return outputStream;
    }
}
