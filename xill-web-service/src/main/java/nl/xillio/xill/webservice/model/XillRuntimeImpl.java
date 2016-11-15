package nl.xillio.xill.webservice.model;

import com.google.inject.Inject;
import com.google.inject.Provider;
import nl.xillio.xill.api.OutputHandler;
import nl.xillio.xill.api.XillEnvironment;

import java.nio.file.Path;
import java.util.Map;

/**
 * Implementation of the {@link XillRuntime}
 *
 * @author Geert Konijnendijk
 */
public class XillRuntimeImpl implements XillRuntime {

    @Override
    public void compile(Path workDirectory, Path robotPath) {

    }

    @Override
    public Object runRobot(Map<String, Object> parameters, OutputHandler outputHandler) {
        return null;
    }

    @Override
    public void abortRobot() {

    }

    @Override
    public void close() {

    }

    @Inject
    public void setXillEnvironmentProvider(Provider<XillEnvironment> xillEnvironmentProvider) {

    }
}
