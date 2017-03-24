package nl.xillio.xill.plugins.date.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.date.data.Date;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;

import static org.testng.Assert.assertEquals;

/**
 * Test class for the IsDate construct of the Date plugin
 *
 * @author Pieter Dirk Soels
 */
public class IsDateConstructTest extends TestUtils {
    /**
     * Test the process method under normal circumstances with a date expression
     */
    @Test
    public void testProcess() {
        // Initialize
        MetaExpression dateExpression = fromValue("Placeholder value");
        dateExpression.storeMeta(new Date(ZonedDateTime.now()));

        // Run
        MetaExpression result = IsDateConstruct.process(dateExpression);

        // Assert
        assertEquals(result.getBooleanValue(), true);
    }

    /**
     * Test the process method under normal circumstances with a non-date expression
     */
    @Test
    public void testNotADate() {
        // Run
        MetaExpression result = IsDateConstruct.process(fromValue(true));

        // Assert
        assertEquals(result.getBooleanValue(), false);
    }

    /**
     * Test the process method with null as input
     */
    @Test
    public void testNullInputDate() {
        // Run
        MetaExpression result = IsDateConstruct.process(ExpressionBuilderHelper.NULL);

        // Assert
        assertEquals(result.getBooleanValue(), false);
    }
}
