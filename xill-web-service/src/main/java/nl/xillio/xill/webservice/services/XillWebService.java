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
package nl.xillio.xill.webservice.services;

import nl.xillio.xill.webservice.exceptions.*;
import nl.xillio.xill.webservice.types.XWID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * This class represents the service for the web controller.
 */
@Service
public class XillWebService {

    private final XillWorkerPoolManagerService workerPoolManagerService;

    @Autowired
    public XillWebService(XillWorkerPoolManagerService workerPoolManagerService) {
        this.workerPoolManagerService = workerPoolManagerService;
    }

    /**
     * Allocate a worker for a specific robot if a space is available.
     *
     * @param robotFQN the robot identificator that should be connected to a worker
     * @return the identifier for the worker
     */
    public XWID allocateWorker(String robotFQN) throws XillBaseException {
        return workerPoolManagerService.getDefaultWorkerPool().allocateWorker(robotFQN).getId();
    }

    /**
     * Release the worker with a specific identifier.
     *
     * @param id the identifier of the worker
     * @throws XillNotFoundException if the worker does not exist.
     * @throws XillInvalidStateException if the worker is not in the required (RUNNING) state.
     */
    public void releaseWorker(XWID id) throws XillNotFoundException, XillInvalidStateException {
        workerPoolManagerService.getDefaultWorkerPool().releaseWorker(id);
    }

    /**
     * Release all workers in all worker pools
     */
    public void releaseAllWorkers() {
        workerPoolManagerService.getDefaultWorkerPool().releaseAllWorkers();
    }

    /**
     * Run existing worker (i.e. run robot associated with the worker)
     *
     * @param id The XillWorker id.
     * @param parameters The parameters used for the associated robot run.
     * @return The result from robot run.
     * @throws XillNotFoundException if the worker does not exist.
     * @throws XillInvalidStateException if the worker is not in the required (IDLE) state.
     */
    public Object runWorker(XWID id, final Map<String, Object> parameters) throws XillNotFoundException, XillInvalidStateException {
        return workerPoolManagerService.getDefaultWorkerPool().runWorker(id, parameters);
    }

    /**
     * Interrupt and stop the running worker (i.e. associated robot).
     *
     * @param id The XillWorker id.
     * @throws XillNotFoundException if the worker does not exist.
     * @throws XillInvalidStateException if the worker is not in the required (RUNNING) state.
     */
    public void stopWorker(XWID id) throws XillNotFoundException, XillInvalidStateException {
        workerPoolManagerService.getDefaultWorkerPool().stopWorker(id);
    }
}
