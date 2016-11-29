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
    public XillWebService(XillWorkerPoolManagerServiceImpl workerPoolManagerService) {
        this.workerPoolManagerService = workerPoolManagerService;
    }

    /**
     * Allocate a worker for a specific robot if a space is available.
     *
     * @param robotFQN the robot identificator that should be connected to a worker
     * @return the identifier for the worker
     */
    public XWID allocateWorker(String robotFQN) throws XillAllocateWorkerException, XillNotFoundException, XillCompileException {
        return workerPoolManagerService.getDefaultWorkerPool().allocateWorker(robotFQN).getId();
    }

    /**
     * Release the worker with a specific identifier.
     *
     * @param id the identifier of the worker
     * @throws XillNotFoundException if the worker does not exist.
     * @throws XillInvalidStateException if the worker is not in the required (RUNNING) state.
     * @throws XillOperationFailedException when the releasing operation fails.
     */
    public void releaseWorker(XWID id) throws XillNotFoundException, XillInvalidStateException, XillOperationFailedException {
        workerPoolManagerService.getDefaultWorkerPool().releaseWorker(id);
    }

    /**
     * Release all workers in all worker pools
     */
    public void releaseAllWorkers() {
        workerPoolManagerService.getAllWorkerPools().forEach(wp -> {
            wp.releaseAllWorkers();
        });
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
