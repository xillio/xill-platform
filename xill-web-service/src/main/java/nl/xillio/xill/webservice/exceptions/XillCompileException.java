package nl.xillio.xill.webservice.exceptions;

/**
 * Exception thrown when {@link nl.xillio.xill.webservice.model.XillRuntime} fails
 * to compile a robot.
 *
 * @author Geert Konijnendijk
 */
public class XillCompileException extends XillBaseException {
    public XillCompileException(String message) {
        super(message);
    }

    public XillCompileException(String message, Throwable cause) {
        super(message, cause);
    }
}
