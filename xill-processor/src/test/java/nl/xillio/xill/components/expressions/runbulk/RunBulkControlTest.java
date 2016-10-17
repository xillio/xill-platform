package nl.xillio.xill.components.expressions.runbulk;

import nl.xillio.xill.api.Debugger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * Tests for {@link RunBulkControl}
 *
 * @author Geert Konijnendijk
 */
public class RunBulkControlTest {

    private RunBulkControl control;

    private Debugger debugger;
    private File robotFile;

    @BeforeMethod
    public void setupControl() {
        debugger = mock(Debugger.class);
        robotFile = mock(File.class);
        control = new RunBulkControl(debugger, robotFile);
    }

    /**
     * Test {@link RunBulkControl#getDebugger()}
     */
    @Test
    public void testGetDebugger() {
        assertSame(control.getDebugger(), debugger);
    }

    /**
     * Test {@link RunBulkControl#getCalledRobotFile()}
     */
    @Test
    public void testGetCalledRobotFile() {
        assertSame(control.getCalledRobotFile(), robotFile);
    }

    /**
     * Test {@link RunBulkControl#getRunCount()} and {@link RunBulkControl#incRunCount()}
     */
    @Test
    public void testGetRunCount() {
        assertEquals(control.getRunCount(), 0);
        control.incRunCount();
        assertEquals(control.getRunCount(), 1);
        control.incRunCount();
        assertEquals(control.getRunCount(), 2);
    }

    /**
     * Test {@link RunBulkControl#shouldStop()} and {@link RunBulkControl#signalStop()}
     */
    @Test
    public void testShouldStop() {
        assertFalse(control.shouldStop());
        control.signalStop();
        assertTrue(control.shouldStop());
    }
}
