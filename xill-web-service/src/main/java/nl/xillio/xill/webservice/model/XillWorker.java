/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.webservice.model;

import nl.xillio.xill.webservice.exceptions.XillCompileException;
import nl.xillio.xill.webservice.exceptions.XillInvalidStateException;
import nl.xillio.xill.webservice.types.XWID;
import org.apache.commons.lang3.concurrent.ConcurrentRuntimeException;

import java.nio.file.Path;
import java.util.Map;

/**
 * This class represents a worker entity in the domain model.
 * A worker can be started which will run a robot given a working directory.
 * This execution can be interrupted on a different thread.
 *
 * @author Xillio
 */
public class XillWorker {

    protected final XWID id;
    protected final Path workDirectory;
    protected final String robotName;

    protected final XillRuntime runtime;

    protected XillWorkerState state;

    protected final Object lock;

    /**
     * Creates a worker for a specific robot.
     *
     * @param runtime       a runtime drawn from the pool
     * @param workDirectory the working directory of the enclosed robot
     * @param robotName     the fully qualified robot name
     * @throws XillCompileException if the robot could not be compiled
     */
    public XillWorker(XillRuntime runtime, Path workDirectory, String robotName) throws XillCompileException {
        id = new XWID();
        this.runtime = runtime;
        this.workDirectory = workDirectory;
        this.robotName = robotName;

        runtime.compile(workDirectory, workDirectory.resolve(robotName));

        state = XillWorkerState.IDLE;

        lock = new Object();
    }

    /**
     * Runs the robotPath associated with the worker.
     *
     * @param arguments the robotPath run arguments
     * @return the result of the robotPath run
     */
    public Object run(final Map<String, Object> arguments) throws XillInvalidStateException {
        synchronized (lock) {
            if (state != XillWorkerState.IDLE) {
                throw new XillInvalidStateException("Worker is not ready for running.");
            }
            state = XillWorkerState.RUNNING;
        }
        try {
            Object returnValue = runtime.runRobot(arguments);
            state = XillWorkerState.IDLE;
            return returnValue;
        } catch (ConcurrentRuntimeException e) {
            state = XillWorkerState.RUNTIME_ERROR;
            throw new XillInvalidStateException("The worker has encountered a problem and cannot continue", e);
        }
    }

    /**
     * Aborts the running worker (i.e. abort the robot associated with the worker).
     */
    public void abort() throws XillInvalidStateException {
        synchronized (lock) {
            if (state != XillWorkerState.RUNNING) {
                throw new XillInvalidStateException("Worker is not running.");
            }
            state = XillWorkerState.ABORTING;
        }
        runtime.abortRobot();
        state = XillWorkerState.IDLE;
    }

    /**
     * Returns the fully qualified robot name.
     *
     * @return the fully qualified robot name
     */
    public String getRobotName() {
        return robotName;
    }

    /**
     * Returns the work directory assigned to this worker.
     *
     * @return the work directory assigned to this worker
     */
    public Path getWorkDirectory() {
        return workDirectory;
    }

    /**
     * Returns the id of the allocated worker, unique across the instances of {@link XillWorkerPool}.
     *
     * @return the id of the allocated worker
     */
    public XWID getId() {
        return id;
    }

    /**
     * Returns the state of the worker.
     *
     * @return the state of the worker
     */
    public XillWorkerState getState() {
        return state;
    }

    /**
     * Returns the worker runtime.
     *
     * @return the worker runtime.
     */
    public XillRuntime getRuntime() {
        return runtime;
    }
}
