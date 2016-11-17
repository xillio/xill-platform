package nl.xillio.xill.webservice.model;


import javax.inject.Provider;

/**
 * Represents a pool of {@link XillRuntime} instances.
 *
 * @author Geert Konijnendijk
 */
public interface XillRuntimePool extends Provider<XillRuntime> {

    /**
     * Retrieve a {@link XillRuntime} from the pool.
     *
     * The returned {@link XillRuntime} is fully initialized, so can be used immediately.
     * When the {@link XillRuntime} is not needed anymore, it should be returned to the pool
     * by calling {@link #returnRuntime(XillRuntime)}.
     *
     * @return A fully initialized runtime
     */
    @Override
    XillRuntime get();

    /**
     * Return a runtime to the pool.
     *
     * The runtime being returned should not be used anymore after calling this method.
     *
     * @param runtime The runtime to be returned
     */
    void returnRuntime(XillRuntime runtime);

    /**
     * Remove a runtime from the pool, ensuring that it will not be returned by {@link #get()} anymore.
     *
     * @param runtime The runtime to remove
     */
    void removeRuntime(XillRuntime runtime);
}
