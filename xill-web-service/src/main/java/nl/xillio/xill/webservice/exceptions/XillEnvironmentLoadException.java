package nl.xillio.xill.webservice.exceptions;

/**
 * Thrown when the loading of the Xill environment fails.
 *
 * This is an unchecked exception since recovering from this exception is not possible
 * without restarting.
 *
 * @author Geert Konijnendijk
 */
public class XillEnvironmentLoadException extends RuntimeException {
    public XillEnvironmentLoadException(String message) {
        super(message);
    }

    public XillEnvironmentLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
