package nl.xillio.xill.api.components;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for the methods and fields of {@link ExpressionBuilderHelper}
 *
 * @author Geert Konijnendijk
 */
public class ExpressionBuilderHelperTest {
    /**
     * Test {@link ExpressionBuilderHelper#TRUE}
     */
    @Test
    public void testTrueExpression() {
        MetaExpression trueExpression = ExpressionBuilderHelper.TRUE;
        assertTrue(trueExpression.getBooleanValue());
        assertEquals(trueExpression.getStringValue(), "true");
        assertEquals(trueExpression.getNumberValue(), 1);
    }

    /**
     * Test {@link ExpressionBuilderHelper#FALSE}
     */
    @Test
    public void testFalseExpression() {
        MetaExpression trueExpression = ExpressionBuilderHelper.FALSE;
        assertFalse(trueExpression.getBooleanValue());
        assertEquals(trueExpression.getStringValue(), "false");
        assertEquals(trueExpression.getNumberValue(), 0);
    }

    /**
     * Test {@link ExpressionBuilderHelper#NULL}
     */
    @Test
    public void testNullExpression() {
        MetaExpression trueExpression = ExpressionBuilderHelper.NULL;
        assertFalse(trueExpression.getBooleanValue());
        assertEquals(trueExpression.getStringValue(), "null");
        assertEquals(trueExpression.getNumberValue(), Double.NaN);
    }

}
