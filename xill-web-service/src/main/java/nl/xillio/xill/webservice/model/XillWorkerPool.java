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

import nl.xillio.xill.webservice.exceptions.*;
import nl.xillio.xill.webservice.types.XWID;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the workerPool of the workers for the given work directory.
 */
public class XillWorkerPool {
    private static final Logger LOGGER = me.biesaart.utils.Log.get();

    private final Path workDirectory;
    private final int poolCardinality;
    private final XWID workerPoolId;
    private final XillWorkerFactory xillWorkerFactory;

    private final Map<XWID, XillWorker> workerPool = new HashMap<>();

    public XillWorkerPool(final Path workDirectory, int poolCardinality, XillWorkerFactory xillWorkerFactory) {
        this.workDirectory = workDirectory;
        this.poolCardinality = poolCardinality;
        this.xillWorkerFactory = xillWorkerFactory;
        this.workerPoolId = new XWID();
    }

    /**
     * Allocate worker for the robot.
     * It can create new worker or reuse free existing worker or throw an exception if no worker can be created and reused.
     *
     * @param robotFQN The robot fully qualified name.
     * @return The allocated worker.
     * @throws XillNotFoundException if the robot was not found.
     * @throws XillAllocateWorkerException there are no resource available for worker allocation.
     * @throws XillNotFoundException when the robot is not found.
     * @throws XillCompileException when the compilation of the robot has failed.
     */
    public XillWorker allocateWorker(final String robotFQN) throws XillBaseException {
        synchronized (workerPool) {
            checkWorkerPoolSize();
        }

         // This operation includes compiling and can last longer time so it must be outside the synchronised block
        XillWorker xillWorker = createWorker(robotFQN);

        synchronized (workerPool) {
            checkWorkerPoolSize(); // The check must be done again to ensure atomic operation on the workerPool
            workerPool.put(xillWorker.getId(), xillWorker);
            return xillWorker;
        }
    }

    XillWorker createWorker(final String robotFQN) throws XillBaseException {
        return xillWorkerFactory.constructWorker(workDirectory, robotFQN);
    }

    void checkWorkerPoolSize() throws XillAllocateWorkerException {
        if (workerPool.size() >= poolCardinality) {
            throw new XillAllocateWorkerException("Could not allocate new worker. The worker workerPool has reached its maximum amount of workers.");
        }
    }

    /**
     * Runs the worker (i.e. robot associated with the worker).
     *
     * @param id The worker identificator.
     * @param parameters The parameters for the robot run.
     * @return The result of the robot run.
     * @throws XillInvalidStateException if the worker is not in IDLE state.
     * @throws XillNotFoundException when the robot is not found.
     */
    public Object runWorker(XWID id, final Map<String, Object> parameters) throws XillInvalidStateException, XillNotFoundException {
        XillWorker xillWorker;
        synchronized (workerPool) {
            xillWorker = findWorker(id);
        }
        // The protection of the xill worker against parallel run and stop operations is implemented in XillWorker itself so no need to deal with it
        return xillWorker.run(parameters);
    }

    /**
     * Stops running worker (i.e. stop robot associated with the worker).
     *
     * @param id The worker identificator.
     * @throws XillNotFoundException when the robot is not found.
     * @throws XillInvalidStateException if the worker is not in RUNNING state.
     */
    public void stopWorker(XWID id) throws XillNotFoundException, XillInvalidStateException {
        XillWorker xillWorker;
        synchronized (workerPool) {
            xillWorker = findWorker(id);
        }
        // The protection of the xill worker against parallel run and stop operations is implemented in XillWorker itself so no need to deal with it
        xillWorker.abort();
    }

    /**
     * Find the worker in the worker workerPool.
     *
     * @param workerId The identifier of the worker.
     * @return The found XillWorker.
     * @throws XillNotFoundException if the worker was not found.
     */
    XillWorker findWorker(XWID workerId) throws XillNotFoundException {
        if (!workerPool.containsKey(workerId)) {
            throw new XillNotFoundException(String.format("The worker %1$d cannot be found.", workerId.getId()));
        }
        return workerPool.get(workerId);
    }

    /**
     * Release the worker in the worker workerPool.
     *
     * @param workerId The identifier of the worker.
     * @throws XillNotFoundException if the worker was not found.
     * @throws XillInvalidStateException if the worker is not in IDLE state.
     */
    public void releaseWorker(XWID workerId) throws XillNotFoundException, XillInvalidStateException {
        synchronized (workerPool) {
            XillWorker xillWorker = findWorker(workerId);
            if (xillWorker.getState() != XillWorkerState.IDLE) {
                throw new XillInvalidStateException(String.format("The worker %1$d cannot be released as it is not in the IDLE state.", xillWorker.getId().getId()));
            }
            xillWorker.close();
            workerPool.remove(xillWorker.getId());
        }
    }

    /**
     * Release all workers in the worker workerPool.
     */
    public void releaseAllWorkers() {
        synchronized (workerPool) {
            workerPool.forEach((xwid, xillWorker) -> xillWorker.close());
            workerPool.clear();
        }
    }

    /**
     * This is helper method for testing.
     *
     * @return the number of items in the worker pool.
     */
    int getPoolSize() {
        return workerPool.size();
    }
}
