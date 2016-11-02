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
