package nl.xillio.xill.webservice.xill;

import nl.xillio.xill.api.XillThreadFactory;

/**
 * Implementation of a {@link XillThreadFactory}. This implementation
 * adds all threads to a single {@link ThreadGroup}. The {@link ThreadGroup} is
 * {@link ThreadGroup#interrupt() interrupted} when this {@link XillThreadFactory} is
 * {@link XillThreadFactory#close() closed}.
 *
 * @author Geert Konijnendijk
 */
public class CleaningXillThreadFactory implements XillThreadFactory {

    private static final String THREAD_GROUP_NAME = "Xill Server Thread Factory";

    private final ThreadGroup group;

    /**
     * Create a new factory with the default {@link ThreadGroup} name
     */
    public CleaningXillThreadFactory() {
        this(THREAD_GROUP_NAME);
    }

    /**
     * Create a new factory
     * @param name The name of the {@link ThreadGroup}
     */
    public CleaningXillThreadFactory(String name) {
        group = new ThreadGroup(name);
        group.setDaemon(true);
    }

    @Override
    public Thread create(Runnable target, String name) {
        return new Thread(group, target, name);
    }

    @Override
    public void close() throws Exception {
        group.interrupt();
        // This method does not destroy the group since there is no guarantee all threads have stopped
    }
}
