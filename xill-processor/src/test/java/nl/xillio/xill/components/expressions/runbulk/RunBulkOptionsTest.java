package nl.xillio.xill.components.expressions.runbulk;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Processable;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link RunBulkOptions}
 *
 * @author Geert Konijnendijk
 */
public class RunBulkOptionsTest extends TestUtils {


    private Processable mockOptions(MetaExpression resultObject) {
        InstructionFlow<MetaExpression> result = mock(InstructionFlow.class);
        when(result.get()).thenReturn(resultObject);
        Processable optionsProcessable = mock(Processable.class);
        when(optionsProcessable.process(any())).thenReturn(result);
        return optionsProcessable;
    }

    @DataProvider
    public Object[][] optionsProvider() {
        return new Object[][] {
                {createMap(), 0, false},
                {createMap("maxThreads", 4), 4, false},
                {createMap("stopOnError", "yes"), 0, true},
                {createMap("stopOnError", "no"), 0, false},
                {createMap("stopOnError", "yes", "maxThreads", 4), 4, true},
                {createMap("stopOnError", "no", "maxThreads", 4), 4, false},
        };
    }

    /**
     * Test {@link RunBulkOptions#RunBulkOptions(Processable)} when no options are given
     */
    @Test(dataProvider = "optionsProvider")
    public void testRunBulkOptions(MetaExpression options, int expectedMaxThreads, boolean expectedShouldStopOnError) {
        // Mock
        Processable optionsProcessable = mockOptions(options);

        // Run
        RunBulkOptions runBulkOptions = new RunBulkOptions(optionsProcessable);

        // Assert
        assertEquals(runBulkOptions.getMaxThreadsVal(), expectedMaxThreads);
        assertEquals(runBulkOptions.shouldStopOnError(), expectedShouldStopOnError);
    }

}
