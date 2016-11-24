package nl.xillio.xill.webservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the loading of the Xill environment fails.
 *
 * This is an unchecked exception since recovering from this exception is not possible
 * without restarting.
 *
 * @author Geert Konijnendijk
 */
@ResponseStatus(code = HttpStatus.SERVICE_UNAVAILABLE, reason = "Cannot load the Xill Environment")
public class XillEnvironmentLoadException extends RuntimeException {
    public XillEnvironmentLoadException(String message) {
        super(message);
    }

    public XillEnvironmentLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
