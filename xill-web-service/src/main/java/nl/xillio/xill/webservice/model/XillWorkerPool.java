package nl.xillio.xill.webservice.model;

import nl.xillio.xill.webservice.exceptions.XillAllocateWorkerException;
import nl.xillio.xill.webservice.exceptions.XillNotFoundException;
import org.apache.commons.lang3.NotImplementedException;

/**
 *
 */
public class XillWorkerPool {
    private final String workDirectory;
    private int maxExecutors = 10;

    public XillWorkerPool(final String workDirectory) {
        this.workDirectory = workDirectory;
    }

    /**
     * Allocate worker for the robot.
     * It can create new worker or reuse free existing worker or throw an exception if no worker can be created and reused.
     *
     * @param robotPath
     * @return
     */
    public XillWorker allocateWorker(final String robotPath) throws XillAllocateWorkerException {
        throw new NotImplementedException("The 'allocateWorker' method has not been implemented yet");
    }

    public XillWorker findWorker(int workerId) throws XillNotFoundException {
        throw new NotImplementedException("The 'findWorker' method has not been implemented yet");
    }

    public void releaseWorker(int workerId) throws XillNotFoundException {
        throw new NotImplementedException("The 'releaseWorker' method has not been implemented yet");
    }

    public void releaseAllWorkers() {
        throw new NotImplementedException("The 'releaseAllWorkers' method has not been implemented yet");
    }

}
