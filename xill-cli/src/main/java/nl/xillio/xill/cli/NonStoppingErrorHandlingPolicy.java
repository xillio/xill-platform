package nl.xillio.xill.cli;

import nl.xillio.xill.api.OutputHandler;
import nl.xillio.xill.api.errors.ErrorHandlingPolicy;

/**
 * Created by Dwight.Peters on 20-Jun-17.
 */
public class NonStoppingErrorHandlingPolicy implements ErrorHandlingPolicy {
    private OutputHandler outputHandler;

    public void handle(final Throwable e){
        sendToOutputHandler(e);
    }

    protected void sendToOutputHandler(Throwable e) {
        if (outputHandler != null) {
            outputHandler.inspect(null, e);
        }
    }
}
