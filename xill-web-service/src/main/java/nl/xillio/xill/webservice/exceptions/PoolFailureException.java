package nl.xillio.xill.webservice.exceptions;

/**
 * Thrown when retrieving or releasing objects from a pool fails.
 *
 * @author Geert Konijnendijk
 */
public class PoolFailureException extends RuntimeException {
    public PoolFailureException(String message) {
        super(message);
    }

    public PoolFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
