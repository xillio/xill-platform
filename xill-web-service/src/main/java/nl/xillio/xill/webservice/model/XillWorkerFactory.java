package nl.xillio.xill.webservice.model;

import nl.xillio.xill.webservice.exceptions.XillBaseException;
import org.apache.commons.pool2.ObjectPool;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.nio.file.Path;

/**
 * A factory for constructing {@link XillWorker workers}.
 *
 * @author Geert Konijnendijk
 */
@Component
public class XillWorkerFactory {

    private ObjectPool<XillRuntime> runtimePool;

    @Inject
    public XillWorkerFactory(ObjectPool<XillRuntime> runtimePool) {
        this.runtimePool = runtimePool;
    }

    /**
     * Construct a {@link XillWorker}.
     *
     * @param workDirectory The working directory
     * @param robotFQN The robot's fully qualified name
     * @return A new {@link XillWorker}
     * @throws XillBaseException When an exception occurs in {@link XillWorker#XillWorker(Path, String, ObjectPool)}
     * @see XillWorker
     */
    public XillWorker constructWorker(Path workDirectory, String robotFQN) throws XillBaseException {
        return new XillWorker(workDirectory, robotFQN, runtimePool);
    }
}
