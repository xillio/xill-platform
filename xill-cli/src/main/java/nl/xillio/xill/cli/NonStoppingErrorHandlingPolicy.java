package nl.xillio.xill.cli;

import nl.xillio.xill.api.errors.ErrorHandlingPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Dwight.Peters on 20-Jun-17.
 */
public class NonStoppingErrorHandlingPolicy implements ErrorHandlingPolicy {

    private final Logger logger = LoggerFactory.getLogger(NonStoppingErrorHandlingPolicy.class);

    public void handle(final Throwable e){
        if (e.getMessage() != null)
        {
            logger.error(e.getMessage(), e);
            return;
        }
        logger.error("An error occured in one of the robots.", e);
    }
}
