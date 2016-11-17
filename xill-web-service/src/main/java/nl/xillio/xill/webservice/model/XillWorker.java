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
package nl.xillio.xill.webservice.model;

import nl.xillio.xill.webservice.types.XWID;
import org.apache.commons.lang3.NotImplementedException;

import java.nio.file.Path;
import java.util.Map;

/**
 * This class represents a worker entity in the domain model.
 * A worker can be started which will run a robotPath. This execution can be interrupted on a different thread.
 *
 * @author Thomas Biesaart
 */
public class XillWorker implements AutoCloseable {
    protected final Path robotPath;

    public XillWorker(Path robotPath) {
        this.robotPath = robotPath;
    }

    public Path getRobotPath() {
        return robotPath;
    }

    /**
     * Return the id of the allocated worker.
     * The id must be unique across the XillWorkerPools.
     *
     * @return
     */
    public XWID getId() {
        throw new NotImplementedException("The 'getId' method has not been implemented yet");
    }

    /**
     * Run the robotPath associated with the worker.
     *
     * @param arguments The robotPath run arguments.
     * @return The result of the robotPath run.
     */
    public Object run(final Map<String, Object> arguments) {
        throw new NotImplementedException("The 'run' method has not been implemented yet");
    }

    /**
     * Abort the running worker (i.e. abort the robot associated with the worker).
     */
    public void abort() {
        throw new NotImplementedException("The 'abort' method has not been implemented yet");
    }

    /**
     * This method's purpose is to return the XillRuntime to the pool.
     *
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        throw new NotImplementedException("The 'close' method has not been implemented yet");
    }
}
