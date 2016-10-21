/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.components.expressions.runbulk;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Processable;
import nl.xillio.xill.api.errors.RobotRuntimeException;
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
                {NULL, 0, false},
                {createMap(), 0, false},
                {createMap("maxThreads", 4), 4, false},
                {createMap("stopOnError", "yes"), 0, true},
                {createMap("stopOnError", "no"), 0, false},
                {createMap("stopOnError", "yes", "maxThreads", 4), 4, true},
                {createMap("stopOnError", "no", "maxThreads", 4), 4, false},
        };
    }

    /**
     * Test {@link RunBulkOptions#RunBulkOptions(Processable)} with various input
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

    /**
     * Test {@link RunBulkOptions#RunBulkOptions(Processable)} when the processable is null
     */
    @Test
    public void testRunBulkOptionsNull() {
        // Run
        RunBulkOptions runBulkOptions = new RunBulkOptions(null);

        // Assert
        assertEquals(runBulkOptions.getMaxThreadsVal(), 0);
        assertEquals(runBulkOptions.shouldStopOnError(), false);
    }

    /**
     * Test {@link RunBulkOptions#RunBulkOptions(Processable)} when the wrong type of options is given
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*The passed value for the \"options\" argument was not an OBJECT.*")
    public void testRunBulkOptionsWrongType() {
        // Mock
        Processable optionsProcessable = mockOptions(createList());

        // Run
        new RunBulkOptions(optionsProcessable);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*A key in the \"options\" argument was not a valid option name.*")
    public void testRunBulkOptionsWrongOption() {
        // Mock
        Processable optionsProcessable = mockOptions(createMap("invalidOption", 3));

        // Run
        new RunBulkOptions(optionsProcessable);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*The \"maxThreads\" value in the \"options\" argument was not valid.*")
    public void testRunBulkOptionsWrongNumThreads() {
        // Mock
        Processable optionsProcessable = mockOptions(createMap("maxThreads", 0));

        // Run
        new RunBulkOptions(optionsProcessable);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*The \"stopOnError\" value in the \"options\" argument was not valid.*")
    public void testRunBulkOptionsWrongStopOnError() {
        // Mock
        Processable optionsProcessable = mockOptions(createMap("stopOnError", "invalid"));

        // Run
        new RunBulkOptions(optionsProcessable);
    }
}
