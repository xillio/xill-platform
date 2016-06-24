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
package nl.xillio.xill.plugins.system.exec;

import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

/**
 * This class will read all characters and returns the result in a string
 */
public class InputStreamReaderCallable implements Callable<String> {
    private final BufferedInputStream input;
    private static final Logger LOGGER = Log.get();

    /**
     * Create a new {@link InputStreamReaderCallable}
     *
     * @param input the input
     */
    public InputStreamReaderCallable(final InputStream input) {
        this.input = new BufferedInputStream(input);
    }

    @Override
    public String call() {
        StringBuilder output = new StringBuilder();

        int chars;
        try {
            while ((chars = input.read()) != -1) {
                output.append((char) chars);
            }
        } catch (IOException e) {
            LOGGER.error("Error while listening to input stream: " + e.getMessage(), e);
        }

        return output.toString();
    }
}
