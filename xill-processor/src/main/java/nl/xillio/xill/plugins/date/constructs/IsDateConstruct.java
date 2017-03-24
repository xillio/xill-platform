package nl.xillio.xill.plugins.date.constructs;


import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.plugins.date.BaseDateConstruct;
import nl.xillio.xill.plugins.date.services.DateService;

/**
 * Determines if the given object is a valid date.
 */
public class IsDateConstruct extends BaseDateConstruct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(IsDateConstruct::process,
                new Argument("date", ATOMIC));
    }

    static MetaExpression process(final MetaExpression dateVar) {
        return fromValue(dateVar.getMeta(Date.class) != null);
    }
}
