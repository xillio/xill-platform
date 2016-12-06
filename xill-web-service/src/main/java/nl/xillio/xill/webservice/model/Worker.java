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

import me.biesaart.utils.Log;
import nl.xillio.xill.webservice.exceptions.*;
import nl.xillio.xill.webservice.types.WorkerID;
import org.apache.commons.lang3.concurrent.ConcurrentRuntimeException;
import org.apache.commons.pool2.ObjectPool;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.file.Path;
import java.util.Map;

/**
 * This class represents a worker entity in the domain model.
 * A worker can be started which will run a robot given by fully qualified name.
 * This execution can be interrupted on a different thread.
 *
 * @author Xillio
 */
public class Worker implements AutoCloseable {

    private static final Logger LOGGER = Log.get();

    protected final WorkerID id;
    protected final Path workDirectory;
    protected final String robotFQN;

    protected final Runtime runtime;
    protected final ObjectPool<Runtime> runtimePool;
    protected final Object lock;
    protected WorkerState state;

    /**
     * Creates a worker for a specific robot.
     *
     * @param workDirectory the working directory of the enclosed robot
     * @param robotFQN      the fully qualified robot name
     * @param runtimePool   The pool holding runtime instances
     * @throws CompileException if the robot could not be compiled
     */
    public Worker(Path workDirectory, String robotFQN, @Qualifier("runtimePool") ObjectPool<Runtime> runtimePool) throws BaseException {
        id = new WorkerID();
        this.runtimePool = runtimePool;

        try {
            this.runtime = runtimePool.borrowObject();
        } catch (Exception e) {
            throw new AllocateWorkerException("Could not retrieve a runtime from the pool", e);
        }

        this.workDirectory = workDirectory;
        this.robotFQN = robotFQN;

        lock = new Object();

        runtime.compile(workDirectory, robotFQN);

        state = WorkerState.IDLE;
    }

    /**
     * Runs the robotPath associated with the worker.
     *
     * @param arguments the robotPath run arguments
     * @return the result of the robotPath run
     */
    public Object run(final Map<String, Object> arguments) throws InvalidStateException {
        synchronized (lock) {
            // Check the worker state
            if (state != WorkerState.IDLE) {
                throw new InvalidStateException("Worker is not ready for running");
            }
            state = WorkerState.RUNNING;
        }

        try {
            // Run a robot
            Object returnValue = runtime.runRobot(arguments);

            // Do not change the state when it is not running any more, it might have been changed while aborting a robot
            synchronized (lock) {
                if (state == WorkerState.RUNNING) {
                    state = WorkerState.IDLE;
                }
            }
            return returnValue;
        } catch (ConcurrentRuntimeException e) {
            state = WorkerState.RUNTIME_ERROR;
            // This worker can not continue, release the runtime
            releaseRuntime();
            throw new InvalidStateException("The worker has encountered a problem and cannot continue", e);
        }
    }

    /**
     * Aborts the running worker (i.e. abort the robot associated with the worker).
     */
    public void abort() throws InvalidStateException {
        synchronized (lock) {
            if (state != WorkerState.RUNNING) {
                throw new InvalidStateException("Worker is not running.");
            }
            state = WorkerState.ABORTING;
        }
        try {
            runtime.abortRobot();
        } catch (RobotAbortException e) {
            state = WorkerState.RUNTIME_ERROR;
            invalidateRuntime();
            throw e;
        }
        state = WorkerState.IDLE;
    }

    @Override
    public void close() {
        if (state == WorkerState.RUNNING) {
            try {
                abort();
            } catch (InvalidStateException e) {
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
            state = WorkerState.RUNTIME_ERROR;
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
            state = WorkerState.RUNTIME_ERROR;
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
     * Returns the id of the allocated worker, unique across the instances of {@link WorkerPool}.
     *
     * @return the id of the allocated worker
     */
    public WorkerID getId() {
        return id;
    }

    /**
     * Returns the state of the worker.
     *
     * @return the state of the worker
     */
    public WorkerState getState() {
        return state;
    }

    /**
     * Returns the worker runtime.
     *
     * @return the worker runtime.
     */
    public Runtime getRuntime() {
        return runtime;
    }
}
