package nl.xillio.xill.webservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a method is called on an object when that object is in invalid state.
 *
 * @author Geert Konijnendijk
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class XillInvalidStateException extends XillBaseException {
    public XillInvalidStateException(String message) {
        super(message);
    }

    public XillInvalidStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
