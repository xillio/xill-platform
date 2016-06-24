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
 * This interface represents a combination of an {@link InputStream} provider and a {@link OutputStream} provider.
 * Note that both the input and output streams are optional.
 *
 * @author Thomas biesaart
 */
public interface IOStream extends AutoCloseable {

    /**
     * Checks whether this IOStream has an input stream.
     *
     * @return whether this IOStream is capable of creating an input stream
     */
    boolean hasInputStream();

    /**
     * Gets the input stream.
     *
     * @return the stream
     * @throws IOException                if the stream could not be created
     * @throws NoStreamAvailableException if there was no stream
     */
    BufferedInputStream getInputStream() throws IOException;

    /**
     * Checks whether this IOStream has an output stream.
     *
     * @return whether this IOStream has an output stream
     */
    boolean hasOutputStream();

    /**
     * Gets the output stream.
     *
     * @return the stream
     * @throws IOException                if the stream could not be created
     * @throws NoStreamAvailableException if there was no stream
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Closes both streams if they are present.
     */
    void close();

    /**
     * Gets the description of this IOStream if there is one.
     *
     * @return the description or null
     */
    String getDescription();
}
