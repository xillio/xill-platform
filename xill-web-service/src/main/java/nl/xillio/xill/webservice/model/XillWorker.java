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

import nl.xillio.xill.webservice.exceptions.XillCompileException;
import nl.xillio.xill.webservice.exceptions.XillInvalidStateException;
import nl.xillio.xill.webservice.types.XWID;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * This class represents a worker entity in the domain model.
 * A worker can be started which will run a robot given a working directory. This execution can be interrupted on a different thread.
 *
 * @author Xillio
 */
public class XillWorker {

    private static final Logger LOGGER = me.biesaart.utils.Log.get();

    protected final XWID id;
    protected final Path workDirectory;
    protected final String robotName;

    protected XillRuntime runtime;

    protected XillWorkerState state;

    /**
     * Creates a worker for a specific robot.
     *
     * @param robotName the fully qualified robot name
     */
    public XillWorker(Path workDirectory, String robotName) {
        id = new XWID();
        this.workDirectory = workDirectory;
        this.robotName = robotName;

        state = XillWorkerState.NEW;
    }

    /**
     * Compiles the robot and readies for running.
     *
     * @throws XillCompileException in case a compilation error occurred
     */
    public synchronized void compile() throws XillCompileException, XillInvalidStateException {
        if (state != XillWorkerState.NEW) {
            throw new XillInvalidStateException("Robot is already compiled.");
        }
        try {
            state = XillWorkerState.COMPILING;
            runtime.compile(workDirectory, Paths.get(robotName));
        } catch (XillCompileException e) {
            state = XillWorkerState.COMPILATION_ERROR;
            throw e;
        }
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

    @Autowired
    public synchronized void setRuntime(XillRuntime runtime) {
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

    /**
     * Returns the state of the worker.
     *
     * @return the state of the worker
     */
    public synchronized XillWorkerState getState() {
        return state;
    }

    /**
     * Runs the robotPath associated with the worker.
     *
     * @param arguments the robotPath run arguments
     * @return the result of the robotPath run
     */
    public synchronized Object run(final Map<String, Object> arguments) throws XillInvalidStateException {
        if (state != XillWorkerState.IDLE) {
            throw new XillInvalidStateException("Worker is not ready for running.");
        }
        state = XillWorkerState.RUNNING;
        Object returnValue = runtime.runRobot(arguments);
        state = XillWorkerState.IDLE;
        return returnValue;
    }

    /**
     * Aborts the running worker (i.e. abort the robot associated with the worker).
     */
    public synchronized void abort() throws XillInvalidStateException {
        if (state != XillWorkerState.RUNNING) {
            throw new XillInvalidStateException("Worker is not running.");
        }
        state = XillWorkerState.ABORTING;
        runtime.abortRobot();
        state = XillWorkerState.IDLE;
    }
}
