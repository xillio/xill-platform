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

import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * This class represents an abstract implementation of the IOStream.
 *
 * @author Thomas biesaart
 */
public abstract class AbstractIOStream implements IOStream {
    private static final Logger LOGGER = Log.get();
    private final String description;

    protected AbstractIOStream(String description) {
        this.description = description;
    }

    @Override
    public void close() {
        if (hasInputStream()) {
            tryClose(this::getInputStream);
        }

        if (hasOutputStream()) {
            tryClose(this::getOutputStream);
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    private void tryClose(StreamWrapper stream) {
        try {
            stream.get().close();
        } catch (Exception e) {
            LOGGER.error("Exception while closing stream", e);
        }
    }

    @FunctionalInterface
    private interface StreamWrapper {
        AutoCloseable get() throws IOException;
    }
}
