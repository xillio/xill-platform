package nl.xillio.xill.webservice.services;

import nl.xillio.xill.webservice.exceptions.XillNotFoundException;
import nl.xillio.xill.webservice.model.XillWorkerPool;
import nl.xillio.xill.webservice.types.XWID;

/**
 * Created by andrea.parrilli on 2016-11-16.
 */
public interface XillWorkerPoolManagerService {
    /**
     * Create new or return existing XillWorkerPool for given projectId.
     *
     * @param projectId The project id. For the moment, it's not used as there is no support for projects yet.
     * @return The XillWorkerPool for given projectId that has been created or reused. For the moment, it always returns one XillWorkerPool as there is no support for projects yet.
     */
    public XillWorkerPool getWorkerPool(final XWID projectId);

    /**
     * Return existing XillWorkerPool for given projectId.
     *
     * @param projectId The project id. For the moment, it's not used as there is no support for projects yet.
     * @return The existing XillWorkerPool for given projectId.
     * @throws XillNotFoundException if XillWorkerPool for given projectId does not exist.
     */
    public XillWorkerPool findWorkerPool(final XWID projectId) throws XillNotFoundException;
}
