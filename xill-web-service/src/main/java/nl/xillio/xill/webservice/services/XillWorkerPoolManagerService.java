package nl.xillio.xill.webservice.services;

import com.google.inject.Singleton;
import nl.xillio.xill.webservice.exceptions.XillNotFoundException;
import nl.xillio.xill.webservice.model.XillWorkerPool;
import org.apache.commons.lang3.NotImplementedException;

/**
 *
 */
@Singleton
public class XillWorkerPoolManagerService {

    /**
     * Create new or return existing XillWorkerPool for given workDirectory.
     *
     * @param workDirectory
     * @return
     */
    public XillWorkerPool getWorkerPool(final String workDirectory) {
        throw new NotImplementedException("The 'getWorkerPool' method has not been implemented yet");
    }

    /**
     * Return existing XillWorkerPool for given workDirectory.
     *
     * @param workDirectory
     * @return The existing XillWorkerPool for given workDirectory.
     * @throws XillNotFoundException if XillWorkerPool for given workDirectory does not exist.
     */
    public XillWorkerPool findWorkerPool(final String workDirectory) throws XillNotFoundException {
        throw new NotImplementedException("The 'findWorkerPool' method has not been implemented yet");
    }
}
