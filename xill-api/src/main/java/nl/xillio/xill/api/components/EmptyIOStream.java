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
package nl.xillio.xill.api.components;

import nl.xillio.xill.api.io.IOStream;
import nl.xillio.xill.api.io.NoStreamAvailableException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents an IOStream that has no providers.
 * Use this for expressions that have no binary representation.
 *
 * @author Thomas biesaart
 */
class EmptyIOStream implements IOStream {
    @Override
    public boolean hasInputStream() {
        return false;
    }

    @Override
    public BufferedInputStream getInputStream() throws IOException {
        throw new NoStreamAvailableException("This is no binary content");
    }

    @Override
    public boolean hasOutputStream() {
        return false;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new NoStreamAvailableException("This is no binary content");
    }

    @Override
    public void close() {
        // There are no streams to close
    }

    @Override
    public String getDescription() {
        return null;
    }
}
