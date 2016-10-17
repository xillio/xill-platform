package nl.xillio.xill.components.expressions.runbulk;

/**
 * Thrown to signal that compilation of a robot in a {@link WorkerRobotFactory} failed
 *
 * @author Geert Konijnendijk
 */
public class WorkerCompileException extends Exception {
    public WorkerCompileException(String message, Throwable cause) {
        super(message, cause);
    }
}
