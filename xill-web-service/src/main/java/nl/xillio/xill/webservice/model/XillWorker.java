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

import nl.xillio.xill.webservice.exceptions.XillInvalidStateException;
import nl.xillio.xill.webservice.types.XWID;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.Map;

/**
 * This class represents a worker entity in the domain model.
 * A worker can be started which will run a robotPath. This execution can be interrupted on a different thread.
 */
public class XillWorker {

    protected XillRuntime runtime;

    protected final Path robotPath;
    protected XillWorkerState state;
    protected final XWID id;

    public XillWorker(Path robotPath) {
        id = new XWID();
        this.robotPath = robotPath;
        state = XillWorkerState.IDLE;
    }

    public Path getRobotPath() {
        return robotPath;
    }

    @Autowired
    public void setRuntime(XillRuntime runtime) {
        this.runtime = runtime;
    }

    /**
     * Returns the id of the allocated worker, unique across the instances of {@link XillWorkerPool}.
     *
     * @return the id of the allocated worker
     */
    public XWID getId() {
        return id;
    }

    public XillWorkerState getState() {
        return state;
    }

    /**
     * Runs the robotPath associated with the worker.
     *
     * @param arguments the robotPath run arguments
     * @return the result of the robotPath run
     */
    public Object run(final Map<String, Object> arguments) {
        state = XillWorkerState.RUNNING;
        Object returnValue = runtime.runRobot(arguments);
        state = XillWorkerState.IDLE;
        return returnValue;
    }

    /**
     * Aborts the running worker (i.e. abort the robot associated with the worker).
     */
    public void abort() throws XillInvalidStateException {
        try {
            state = XillWorkerState.ABORTING;
            runtime.abortRobot();
        } catch (XillInvalidStateException e) {
            throw e;
        }
        state = XillWorkerState.IDLE;
    }
}
