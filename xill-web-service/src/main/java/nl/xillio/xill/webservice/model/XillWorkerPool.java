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
import nl.xillio.xill.webservice.xill.XillRuntimeImpl;
import org.slf4j.Logger;
import org.springframework.aop.target.CommonsPool2TargetSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the workerPool of the workers for given work directory.
 */
public class XillWorkerPool {
    private static final Logger LOGGER = me.biesaart.utils.Log.get();

    protected final Path workDirectory;
    protected final int poolCardinality;
    protected final XWID workerPoolId;
    protected final CommonsPool2TargetSource xillRuntimePool;

    private final Map<Integer, XillWorker> workerPool = new HashMap<>();

    @Autowired
    public XillWorkerPool(final Path workDirectory, int poolCardinality, @Qualifier("xillRuntimePool") CommonsPool2TargetSource xillRuntimePool) {
        this.workDirectory = workDirectory;
        this.poolCardinality = poolCardinality;
        this.workerPoolId = new XWID();
        this.xillRuntimePool = xillRuntimePool;
    }

    /**
     * Allocate worker for the robot.
     * It can create new worker or reuse free existing worker or throw an exception if no worker can be created and reused.
     *
     * @param robotFQN The robot fully qualified name.
     * @return The allocated worker.
     * @throws XillNotFoundException if the robot was not found.
     * @throws XillAllocateWorkerException there are not resource available for worker allocation.
     * @throws XillNotFoundException when the robot is not found.
     * @throws XillCompileException when the compilation of the robot has failed.
     */
    public XillWorker allocateWorker(final String robotFQN) throws XillAllocateWorkerException, XillNotFoundException, XillCompileException {
        synchronized (workerPool) {
            checkWorkerPoolSize();
        }

         // This operation includes compiling and can last longer time so it must be outside the synchronised block
        XillWorker xillWorker;
        try {
            xillWorker = new XillWorker((XillRuntimeImpl)xillRuntimePool.getTarget(), workDirectory, robotFQN);
        } catch (Exception e) {
            throw new XillAllocateWorkerException(String.format("Could not allocate runtime for worker %1$d.", e));
        }

        synchronized (workerPool) {
            checkWorkerPoolSize(); // The check must be done again to be atomic operation on the workerPool
            workerPool.put(xillWorker.getId().getId(), xillWorker);
            return xillWorker;
        }
    }

    private void checkWorkerPoolSize() throws XillAllocateWorkerException {
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
    private XillWorker findWorker(XWID workerId) throws XillNotFoundException {
        if (!workerPool.containsKey(workerId.getId())) {
            throw new XillNotFoundException(String.format("The worker %1$d cannot be found.", workerId.getId()));
        }
        return workerPool.get(workerId.getId());
    }

    private void releaseXillRuntime(XillWorker xillWorker) throws XillOperationFailedException {
        try {
            xillRuntimePool.releaseTarget(xillWorker.getRuntime());
        } catch (Exception e) {
            throw new XillOperationFailedException("The releasing of the worker failed.", e);
        }
    }

    /**
     * Release the worker in the worker workerPool.
     *
     * @param workerId The identifier of the worker.
     * @throws XillNotFoundException if the worker was not found.
     * @throws XillInvalidStateException if the worker is not in IDLE state.
     * @throws XillOperationFailedException when the releasing operation fails.
     */
    public void releaseWorker(XWID workerId) throws XillNotFoundException, XillInvalidStateException, XillOperationFailedException {
        synchronized (workerPool) {
            XillWorker xillWorker = findWorker(workerId);
            if (xillWorker.getState() != XillWorkerState.IDLE) {
                throw new XillInvalidStateException(String.format("The worker %1$d cannot be released as it is not in the IDLE state.", workerId.getId()));
            }
            releaseXillRuntime(xillWorker);
            workerPool.remove(xillWorker);
        }
    }

    /**
     * Release all workers in the worker workerPool.
     */
    public void releaseAllWorkers() {
        synchronized (workerPool) {
            workerPool.forEach((id, xillWorker) -> {
                if (xillWorker.getState() == XillWorkerState.RUNNING) {
                    try {
                        LOGGER.warn(String.format("Aborting the worker %1$d.", xillWorker.getId().getId()));
                        xillWorker.abort();
                        releaseXillRuntime(xillWorker);
                    } catch (XillInvalidStateException e) {
                        LOGGER.error(String.format("Could not start aborting the worker %1$d", xillWorker.getId().getId()));
                    } catch (XillOperationFailedException e) {
                        LOGGER.error(String.format("Could not release the worker %1$d", xillWorker.getId().getId()));
                    }
                }
            });
        }
    }
}
