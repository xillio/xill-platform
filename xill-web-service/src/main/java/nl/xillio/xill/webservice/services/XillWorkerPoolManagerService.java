package nl.xillio.xill.webservice.services;

import nl.xillio.xill.webservice.model.XillWorkerPool;
import nl.xillio.xill.webservice.types.XWID;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Represents a service that manages {@link XillWorkerPool}: factory and reference.
 */
public interface XillWorkerPoolManagerService {
    /**
     * Create new or return existing XillWorkerPool for given projectId.
     *
     * @param workDirectory The project directory. For the moment, it's not used as there is no support for projects yet.
     * @return The XillWorkerPool for given workDirectory that has been created or reused. For the moment, it always returns one XillWorkerPool as there is no support for projects yet.
     */
    public XillWorkerPool getWorkerPool(final Path workDirectory);

    /**
     * Get the default {@link XillWorkerPool} based on configuration. The worker pool is instantiated if necessary.
     *
     * @return the default worker pool for this service
     */
    public XillWorkerPool getDeafultWorkerPool();

    /**
     * Return existing XillWorkerPool for given projectId.
     *
     * @param projectId The project id. For the moment, it's not used as there is no support for projects yet.
     * @return The existing XillWorkerPool for given projectId.
     */
    public Optional<XillWorkerPool> findWorkerPool(final XWID projectId);
}
