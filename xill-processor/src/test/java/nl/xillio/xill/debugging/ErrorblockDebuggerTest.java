package nl.xillio.xill.debugging;

import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.Instruction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * @author Geert Konijnendijk
 */
public class ErrorblockDebuggerTest {

    private Debugger parentDebugger;
    private ErrorBlockDebugger debugger;
    private Instruction lastInstruction;

    @BeforeMethod
    public void setup() {
        parentDebugger = mock(Debugger.class);
        List<Instruction> stackTrace = new ArrayList<>();
        lastInstruction = mock(Instruction.class);
        stackTrace.add(mock(Instruction.class));
        stackTrace.add(lastInstruction);
        when(parentDebugger.getStackTrace()).thenReturn(stackTrace);
        debugger = new ErrorBlockDebugger(parentDebugger);
    }

    /**
     * Create a new throwable and spy it. The throwable is spied to check suppressed throwables.
     * @return A spied throwable
     */
    private Throwable mockThrowable() {
        Throwable throwable = mock(Throwable.class);
        // Add stacktrace so logging won't fail
        when(throwable.getStackTrace()).thenReturn(new StackTraceElement[]{});
        return throwable;
    }

    /**
     * Test {@link ErrorBlockDebugger#handle(Throwable)} under normal circumstances
     */
    @Test
    public void testHandleOneError() {
        // Mock
        Throwable throwable = mockThrowable();

        // Run
        debugger.handle(throwable);

        // Assert
        assertSame(debugger.getError(), throwable);
        assertSame(debugger.getErroredInstruction(), lastInstruction);
    }

    /**
     * Test {@link ErrorBlockDebugger#handle(Throwable)} when multiple errors occur before
     * {@link ErrorBlockDebugger#getError()} is called.
     */
    @Test
    public void testHandleMultipleErrors() {
        // Mock

        // Spy this throwable to assert suppressed errors later
        Throwable throwable1 = spy(Throwable.class);
        Throwable throwable2 = mockThrowable();
        Throwable throwable3 = mockThrowable();


        // Run
        debugger.handle(throwable1);
        debugger.handle(throwable2);
        debugger.handle(throwable3);

        // Throwable.addSuppressed() is final, so can not be verified

        // Assert
        assertEquals(throwable1.getSuppressed(), new Throwable[]{throwable2, throwable3});
        assertSame(debugger.getError(), throwable1);
        assertSame(debugger.getErroredInstruction(), lastInstruction);
    }

    /**
     * Test {@link ErrorBlockDebugger#getError()} when no error is present
     */
    @Test(expectedExceptions = NoSuchElementException.class)
    public void testGetErrorNoError() {
        debugger.getError();
    }

    @DataProvider
    public Object[][] conditions() {
        return new Object[][] {{true, null, true}, {false, null, false}, {true, mockThrowable(), true}, {false, mockThrowable(), true}};
    }

    /**
     * Test {@link ErrorBlockDebugger#hasError()} and {@link ErrorBlockDebugger#shouldStop()} under all conditions
     */
    @Test(dataProvider = "conditions")
    public void testConditions(boolean shouldStop, Throwable throwable, boolean expected) {

        if (throwable != null) {
            debugger.handle(throwable);
        }

        when(debugger.shouldStop()).thenReturn(shouldStop);

        // Run
        boolean hasErrorResult = debugger.hasError();
        boolean shouldStopResult = debugger.shouldStop();

        // Assert
        assertEquals(hasErrorResult, throwable!=null);
        assertEquals(shouldStopResult, expected);
    }

    @Test
    public void testGetErroredInstructionEmptyStackTrace() {
        // Mock
        when(parentDebugger.getStackTrace()).thenReturn(new ArrayList<>());
        Throwable throwable = mockThrowable();

        // Run
        debugger.handle(throwable);
        Instruction result = debugger.getErroredInstruction();

        // Assert
        assertNull(result);
    }
}
