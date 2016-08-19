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
package nl.xillio.exiftool.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class represents the result of a call to the process.
 * It will cache a single line from a {@link BufferedReader}.
 *
 * @author Thomas Biesaart
 */
public class ExecutionResult implements Iterator<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionResult.class);
    private final BufferedReader reader;
    private final StatusCallback statusCallback;
    private final String killString;
    private String cachedLine;
    private boolean shouldClose;

    /**
     * Create a new ExecutionResult.
     *
     * @param reader         the reader the read the output from
     * @param statusCallback the callback that should be called when reading the output is done
     * @param killString     the string that marks the end of this output
     */
    public ExecutionResult(BufferedReader reader, StatusCallback statusCallback, String killString) {
        this.reader = reader;
        this.statusCallback = statusCallback;
        this.killString = killString;
        cacheNext();
    }

    private void cacheNext() {
        try {
            cachedLine = reader.readLine();

            if (cachedLine == null || cachedLine.equals(killString)) {
                shouldClose = true;
                statusCallback.releaseProcess();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read next line", e);
            cachedLine = null;
            shouldClose = true;
        }
    }

    @Override
    public boolean hasNext() {
        return !shouldClose && cachedLine != null;
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more elements in this result");
        }

        String current = cachedLine;
        cacheNext();
        return current;
    }

    public void close() {
        // Read all data
        while (!shouldClose) {
            cacheNext();
        }
    }
}
