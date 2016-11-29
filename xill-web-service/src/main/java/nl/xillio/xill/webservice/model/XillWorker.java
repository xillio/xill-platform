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

import me.biesaart.utils.Log;
import nl.xillio.xill.webservice.exceptions.*;
import nl.xillio.xill.webservice.types.XWID;
import org.apache.commons.lang3.concurrent.ConcurrentRuntimeException;
import org.apache.commons.pool2.ObjectPool;
import org.slf4j.Logger;
import org.springframework.aop.target.AbstractPoolingTargetSource;
import org.springframework.aop.target.CommonsPool2TargetSource;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Provider;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * This class represents a worker entity in the domain model.
 * A worker can be started which will run a robot given a working directory.
 * This execution can be interrupted on a different thread.
 *
 * @author Xillio
 */
public class XillWorker implements AutoCloseable {

    private static final Logger LOGGER = Log.get();

    protected final XWID id;
    protected final Path workDirectory;
    protected final String robotFQN;

    protected final XillRuntime runtime;
    protected final ObjectPool<XillRuntime> runtimePool;
    protected final Object lock;
    protected XillWorkerState state;

    /**
     * Creates a worker for a specific robot.
     *
     * @param workDirectory the working directory of the enclosed robot
     * @param robotFQN      the fully qualified robot name
     * @param runtimePool   The pool holding runtime instances
     * @throws XillCompileException if the robot could not be compiled
     */
    public XillWorker(Path workDirectory, String robotFQN, @Qualifier("runtimePool") ObjectPool<XillRuntime> runtimePool) throws XillBaseException {
        id = new XWID();
        this.runtimePool = runtimePool;

        try {
            this.runtime = runtimePool.borrowObject();
        } catch (Exception e) {
            throw new XillAllocateWorkerException("Could not retrieve a runtime from the pool", e);
        }

        this.workDirectory = workDirectory;
        this.robotFQN = robotFQN;

        lock = new Object();

        runtime.compile(workDirectory, robotFQN);

        state = XillWorkerState.IDLE;
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
            // Do not change the state when it is not running any more, it might have been changed while aborting a robot
            if (state == XillWorkerState.RUNNING) {
                state = XillWorkerState.IDLE;
            }
            return returnValue;
        } catch (ConcurrentRuntimeException e) {
            state = XillWorkerState.RUNTIME_ERROR;
            // This worker can not continue, release the runtime
            releaseRuntime();
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
        try {
            runtime.abortRobot();
        } catch (RobotAbortException e) {
            state = XillWorkerState.RUNTIME_ERROR;
            invalidateRuntime();
            return;
        }
        state = XillWorkerState.IDLE;
    }

    @Override
    public void close() {
        if (state == XillWorkerState.RUNNING) {
            try {
                abort();
            } catch (XillInvalidStateException e) {
                LOGGER.error("The robot has already been aborted", e);
            }
        }
        releaseRuntime();
    }

    /**
     * Invalidate the runtime from the pool so it will not be used again.
     */
    private void invalidateRuntime() {
        try {
            runtimePool.invalidateObject(runtime);
        } catch (Exception e) {
            state = XillWorkerState.RUNTIME_ERROR;
            throw new PoolFailureException("Pool could not invalidate the runtime", e);
        }
    }

    /**
     * Return the runtime to the pool
     */
    private void releaseRuntime() {
        try {
            runtimePool.returnObject(runtime);
        } catch (Exception e) {
            state = XillWorkerState.RUNTIME_ERROR;
            throw new PoolFailureException("Could not return the runtime to the pool", e);
        }
    }

    /**
     * Returns the fully qualified robot name.
     *
     * @return the fully qualified robot name
     */
    public String getRobotFQN() {
        return robotFQN;
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
}
