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

import java.io.IOException;

/**
 * This interface represents an instance of an exiftool process.
 *
 * @author Thomas Biesaart
 */
public interface ExifToolProcess extends AutoCloseable {
    /**
     * Check if this process needs initialization.
     *
     * @return true if this process required initialization
     */
    boolean needInit();

    /**
     * Initialize the program.
     *
     * @throws IOException if initialization fails
     */
    void init() throws IOException;

    /**
     * Check if this process is currently running a query.
     *
     * @return true if this process is running a query
     */
    boolean isRunning();

    /**
     * Check if this process has been closed.
     *
     * @return true if and only if this process has closed
     */
    boolean isClosed();

    /**
     * Check if this process is ready to execute a query.
     *
     * @return true if and only if this process is available
     */
    boolean isAvailable();

    /**
     * Start this process.
     *
     * @throws IOException if execution failed
     */
    void start() throws IOException;

    /**
     * Close this process.
     */
    @Override
    void close();

    /**
     * Run arguments on this process.
     *
     * @param arguments the arguments
     * @return the line iterator containing the string output of the query
     * @throws IOException           if execution fails
     * @throws IllegalStateException if this process is already running
     */
    ExecutionResult run(String... arguments) throws IOException;
}
