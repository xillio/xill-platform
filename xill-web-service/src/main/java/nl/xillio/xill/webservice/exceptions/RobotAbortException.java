package nl.xillio.xill.webservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when aborting a robot fails.
 *
 * @author Geert Konijnendijk
 */
@ResponseStatus(code = HttpStatus.CONFLICT, reason = "The robot could not be aborted")
public class RobotAbortException extends RuntimeException {
    public RobotAbortException(String message) {
        super(message);
    }

    public RobotAbortException(String message, Throwable cause) {
        super(message, cause);
    }
}
