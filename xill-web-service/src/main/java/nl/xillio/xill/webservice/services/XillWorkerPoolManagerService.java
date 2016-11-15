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
     * Create new or return existing XillWorkerPool for given projectId.
     *
     * @param projectId The project id. For the moment, it's not used as there is no support for projects yet.
     * @return The XillWorkerPool for given projectId that has been created or reused. For the moment, it always returns one XillWorkerPool as there is no support for projects yet.
     */
    public XillWorkerPool getWorkerPool(final String projectId) {
        throw new NotImplementedException("The 'getWorkerPool' method has not been implemented yet");
    }

    /**
     * Return existing XillWorkerPool for given projectId.
     *
     * @param projectId The project id. For the moment, it's not used as there is no support for projects yet.
     * @return The existing XillWorkerPool for given projectId.
     * @throws XillNotFoundException if XillWorkerPool for given projectId does not exist.
     */
    public XillWorkerPool findWorkerPool(final String projectId) throws XillNotFoundException {
        throw new NotImplementedException("The 'findWorkerPool' method has not been implemented yet");
    }
}
