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

import nl.xillio.xill.webservice.exceptions.XillAllocateWorkerException;
import nl.xillio.xill.webservice.exceptions.XillNotFoundException;
import nl.xillio.xill.webservice.types.XWID;
import org.apache.commons.lang3.NotImplementedException;

import java.nio.file.Path;

/**
 * This class represents the pool of the workers.
 */
public class XillWorkerPool {
    protected final Path workDirectory;
    protected final int poolCardinality;
    protected final XWID workerPoolId;

    public XillWorkerPool(final Path workDirectory, int poolCardinality) {
        this.workDirectory = workDirectory;
        this.poolCardinality = poolCardinality;
        this.workerPoolId = new XWID();

        //System.out.println(String.format("MaxExecutors: %1$d", properties.getMaxExecutors()));
    }

    /**
     * Allocate worker for the robot.
     * It can create new worker or reuse free existing worker or throw an exception if no worker can be created and reused.
     *
     * @param robotFQN
     * @return
     * @throws XillNotFoundException if the robot was not found.
     * @throws XillAllocateWorkerException there are not resource available for worker allocation
     */
    public XillWorker allocateWorker(final String robotFQN) throws XillAllocateWorkerException, XillNotFoundException {
        throw new NotImplementedException("The 'allocateWorker' method has not been implemented yet");
    }

    /**
     * Find the worker in the worker pool.
     *
     * @param workerId The identifier of the worker.
     * @return Found XillWorker.
     * @throws XillNotFoundException if the worker was not found.
     */
    public XillWorker findWorker(XWID workerId) throws XillNotFoundException {
        throw new NotImplementedException("The 'findWorker' method has not been implemented yet");
    }

    /**
     * Release the worker in the worker pool.
     *
     * @param workerId The identifier of the worker.
     * @throws XillNotFoundException if the worker was not found.
     */
    public void releaseWorker(XWID workerId) throws XillNotFoundException {
        throw new NotImplementedException("The 'releaseWorker' method has not been implemented yet");
    }

    /**
     * Release all workers in the worker pool.
     */
    public void releaseAllWorkers() {
        throw new NotImplementedException("The 'releaseAllWorkers' method has not been implemented yet");
    }
}
