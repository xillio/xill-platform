package ${package}.constructs;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import org.slf4j.Logger;

/**
 * This is an automatically generated construct.
 * It will print a greeting to the console.
 *
 * @see <a href="http://guide.xill.io">http://guide.xill.io</a>
 * @since ${version}
 * @author ${author}
 */
public class HelloConstruct extends Construct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                text -> process(text, context.getRootLogger()),
                new Argument("name", ATOMIC)
        );
    }

    private MetaExpression process(MetaExpression name, Logger logger) {
        logger.info("Hello {}!", name.getStringValue());
        return NULL;
    }
}
