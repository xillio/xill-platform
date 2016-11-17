package nl.xillio.xill.webservice.xill;

import nl.xillio.xill.webservice.XillRuntimeConfiguration;
import nl.xillio.xill.webservice.model.XillRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

/**
 * Integration tests for the {@link XillRuntimeImpl} and pooling.
 *
 * @author Geert Konijnendijk
 */
@ContextConfiguration(classes = { XillRuntimeConfiguration.class, XillRuntimeImpl.class,
        XillEnvironmentFactory.class, XillRuntimeProperties.class, Log4JOutputHandler.class})
public class XillRuntimeIT extends AbstractTestNGSpringContextTests {

    @Test
    @Autowired
    public void testRunRobot(XillRuntime xillRuntime) {
        logger.debug(xillRuntime);
    }

}
