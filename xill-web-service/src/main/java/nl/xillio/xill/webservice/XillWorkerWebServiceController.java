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
package nl.xillio.xill.webservice;

import nl.xillio.xill.webservice.exceptions.XillNotFoundException;
import nl.xillio.xill.webservice.model.XillWorker;
import nl.xillio.xill.webservice.services.XillWorkerPoolManagerServiceImpl;
import nl.xillio.xill.webservice.types.XWID;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class represents the main API controller. It is responsible for interacting with the actor that
 * calls the web API.
 *
 * @author Thomas Biesaart
 */
public class XillWorkerWebServiceController {

    @Autowired
    private XillWorkerPoolManagerServiceImpl workerPoolManagerService;

    /**
     * Register a worker for a specific robot if a space is available.
     *
     * @param worker the worker that should be registered
     * @return the identifier for the worker
     */
    public XWID registerWorker(XillWorker worker) {
        throw new NotImplementedException("The 'registerWorker' method has not been implemented yet");
    }

    /**
     * Release the worker with a specific identifier.
     *
     * @param id the identifier of the worker
     */
    public void releaseWorker(XWID id) {
        try {
            workerPoolManagerService.findWorkerPool(null).releaseWorker(id);
        } catch (XillNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void releaseAllWorkers() {
        throw new NotImplementedException("The 'releaseAllWorkers' method has not been implemented yet");
    }

    public Object runWorker(XWID id) {
        throw new NotImplementedException("The 'runWorker' method has not been implemented yet");
    }

    public void abortWorker(XWID id) {
        try {
            workerPoolManagerService.findWorkerPool(null).findWorker(id).abort();
        } catch (XillNotFoundException e) {
            e.printStackTrace();
        }
    }
}
