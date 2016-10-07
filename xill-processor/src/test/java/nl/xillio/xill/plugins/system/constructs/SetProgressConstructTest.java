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
package nl.xillio.xill.plugins.system.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.plugins.system.services.info.ProgressTrackerService;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * Test the {@link SetProgressConstruct}
 */
public class SetProgressConstructTest extends TestUtils {

    @Test
    public void testProcessNormalUsageWOOptions() throws NoSuchFieldException {
        // Mock
        MetaExpression progress = mockExpression(ATOMIC);
        Number progressValue = 0.5;
        when(progress.getNumberValue()).thenReturn(progressValue);

        MetaExpression options = mock(MetaExpression.class);
        when(options.isNull()).thenReturn(true);

        ConstructContext context = mock(ConstructContext.class);
        Debugger debugger = mock(Debugger.class);
        when(context.getDebugger()).thenReturn(debugger);
        when(context.getCompilerSerialId()).thenReturn(new UUID(1,1));

        SetProgressConstruct construct = new SetProgressConstruct(new ProgressTrackerService());

        // Run the method
        MetaExpression result = construct.process(progress, options, context);

        // Verify
        verify(context, times(1)).getDebugger();

        // Make assertions
        assertEquals(result.getBooleanValue(), true);
    }

    @Test
    public void testProcessWhenDebuggerIsNull() {
        // Mock
        MetaExpression progress = mockExpression(ATOMIC);
        Number progressValue = 0.5;
        when(progress.getNumberValue()).thenReturn(progressValue);

        MetaExpression options = mock(MetaExpression.class);
        when(options.isNull()).thenReturn(true);

        ConstructContext context = mock(ConstructContext.class);
        when(context.getDebugger()).thenReturn(null);

        // Run the method
        MetaExpression result = SetProgressConstruct.process(progress, options, context);

        // Verify
        verify(context).getDebugger();

        // Make assertions
        assertEquals(result.getBooleanValue(), false);
    }

    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Invalid progress value type\\..*")
    public void testProcessWhenInvalidProgressValue() {
        // Mock
        MetaExpression progress = mockExpression(ATOMIC);
        Number progressValue = Double.NaN;
        when(progress.getNumberValue()).thenReturn(progressValue);

        MetaExpression options = mock(MetaExpression.class);
        when(options.isNull()).thenReturn(true);

        ConstructContext context = mock(ConstructContext.class);
        Debugger debugger = mock(Debugger.class);
        when(context.getDebugger()).thenReturn(debugger);

        // Run the method
        SetProgressConstruct.process(progress, options, context);
    }

    private void testProcessNormalUsageWOptions(final String option, final String value) {
        // Mock
        MetaExpression progress = mockExpression(ATOMIC);
        Number progressValue = 0.5;
        when(progress.getNumberValue()).thenReturn(progressValue);

        MetaExpression options = mock(MetaExpression.class);
        when(options.isNull()).thenReturn(false);
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        optionsValue.put(option, fromValue(value));
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(OBJECT);

        ConstructContext context = mock(ConstructContext.class);
        Debugger debugger = mock(Debugger.class);
        when(context.getDebugger()).thenReturn(debugger);
        when(context.getCompilerSerialId()).thenReturn(new UUID(1,1));

        SetProgressConstruct construct = new SetProgressConstruct(new ProgressTrackerService());

        // Run the method
        MetaExpression result = construct.process(progress, options, context);

        // Verify
        verify(context, times(1)).getDebugger();

        // Make assertions
        assertEquals(result.getBooleanValue(), true);
    }

    @Test
    public void testProcessNormalUsageWOptionsHide() {
        testProcessNormalUsageWOptions("onStop", "hide");
    }

    @Test
    public void testProcessNormalUsageWOptionsLeave() {
        testProcessNormalUsageWOptions("onStop", "leave");
    }

    @Test
    public void testProcessNormalUsageWOptionsZero() {
        testProcessNormalUsageWOptions("onStop", "zero");
    }

    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Invalid option\\..*")
    public void testProcessNormalUsageWInvalidOptions() {
        testProcessNormalUsageWOptions("invalid", "nothing");
    }

    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Invalid option value\\..*")
    public void testProcessNormalUsageWInvalidOptionsB() {
        testProcessNormalUsageWOptions("onStop", "invalidValue");
    }
}
