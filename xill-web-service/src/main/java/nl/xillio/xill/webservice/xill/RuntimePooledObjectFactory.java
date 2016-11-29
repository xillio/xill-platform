package nl.xillio.xill.webservice.xill;

import nl.xillio.xill.webservice.model.XillRuntime;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Factory for pooled {@link XillRuntime} instances.
 *
 * @author Geert Konijnendijk
 */
@Component
public class RuntimePooledObjectFactory extends BasePooledObjectFactory<XillRuntime> {

    private Provider<XillRuntime> xillRuntimeProvider;

    @Inject
    public RuntimePooledObjectFactory(Provider<XillRuntime> xillRuntimeProvider) {
        this.xillRuntimeProvider = xillRuntimeProvider;
    }

    @Override
    public XillRuntime create() throws Exception {
        return xillRuntimeProvider.get();
    }

    @Override
    public PooledObject<XillRuntime> wrap(XillRuntime obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public void destroyObject(PooledObject<XillRuntime> p) throws Exception {
        p.getObject().close();
    }
}
