package nl.xillio.xill.webservice.exceptions;

import nl.xillio.xill.api.Issue;

import java.util.List;

/**
 * Exception thrown when {@link nl.xillio.xill.webservice.model.XillRuntime} fails
 * to compile a robot.
 *
 * @author Geert Konijnendijk
 */
public class XillCompileException extends XillBaseException {

    private List<Issue> issues;

    public XillCompileException(String message, Throwable cause) {
        super(message, cause);
    }

    public XillCompileException(String message, List<Issue> issues) {
        super(message);
        this.issues = issues;
    }

    public List<Issue> getIssues() {
        return issues;
    }
}
