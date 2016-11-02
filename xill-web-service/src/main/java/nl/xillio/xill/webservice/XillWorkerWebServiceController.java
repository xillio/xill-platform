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

import nl.xillio.xill.webservice.model.Worker;
import org.apache.commons.lang3.NotImplementedException;

/**
 * This class represents the main API controller. It is responsible for interacting with the actor that
 * calls the web API.
 *
 * @author Thomas Biesaart
 */
public class XillWorkerWebServiceController {

    /**
     * Register a worker for a specific robot if a space is available.
     *
     * @param worker the worker that should be registered
     * @return the identifier for the worker
     */
    public int registerWorker(Worker worker) {
        throw new NotImplementedException("The 'registerWorker' method has not been implemented yet");
    }

    /**
     * Release the worker with a specific identifier.
     *
     * @param id the identifier of the worker
     */
    public void releaseWorker(int id) {
        throw new NotImplementedException("The 'registerWorker' method has not been implemented yet");
    }

    public void releaseAllWorkers() {
        throw new NotImplementedException("The 'releaseAllWorkers' method has not been implemented yet");
    }

    public Object runWorker(int id) {
        throw new NotImplementedException("The 'runWorker' method has not been implemented yet");
    }

    public void interruptWorker(int id) {
        throw new NotImplementedException("The 'registerWorker' method has not been implemented yet");
    }
}
