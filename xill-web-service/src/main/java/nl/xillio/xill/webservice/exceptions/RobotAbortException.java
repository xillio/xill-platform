package nl.xillio.xill.webservice.exceptions;

/**
 * Thrown when aborting a robot fails.
 *
 * @author Geert Konijnendijk
 */
public class RobotAbortException extends RuntimeException {
    public RobotAbortException(String message) {
        super(message);
    }

    public RobotAbortException(String message, Throwable cause) {
        super(message, cause);
    }
}
