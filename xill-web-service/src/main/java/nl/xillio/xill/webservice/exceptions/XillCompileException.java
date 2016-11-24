package nl.xillio.xill.webservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when {@link nl.xillio.xill.webservice.model.XillRuntime} fails
 * to compile a robot.
 *
 * @author Geert Konijnendijk
 */
@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "The robot does not compile")
public class XillCompileException extends XillBaseException {
    public XillCompileException(String message) {
        super(message);
    }

    public XillCompileException(String message, Throwable cause) {
        super(message, cause);
    }
}
