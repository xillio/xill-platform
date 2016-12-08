package nl.xillio.xill.webservice.xill;

import nl.xillio.xill.api.errors.ErrorHandlingPolicy;

/**
 * XWS specific implementation for handling errors in Xill robots.
 *
 * @author Geert Konijnendijk
 */
public class RobotErrorHandler implements ErrorHandlingPolicy {

    @Override
    public void handle(Throwable e) {
        // no-op, simply ignore errors
    }
}
