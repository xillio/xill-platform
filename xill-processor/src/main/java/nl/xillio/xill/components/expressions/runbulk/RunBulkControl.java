package nl.xillio.xill.components.expressions.runbulk;

import nl.xillio.xill.api.Debugger;

import java.io.File;

/**
 * Controls the {@link RunBulkExpression} threads.
 */
class RunBulkControl {
    private int runCount = 0;
    private boolean stop = false;
    private final Debugger debugger;
    private final File calledRobotFile;

    public RunBulkControl(final Debugger debugger, final File calledRobotFile) {
        this.debugger = debugger;
        this.calledRobotFile = calledRobotFile;
    }

    public Debugger getDebugger() {
        return debugger;
    }

    public File getCalledRobotFile() {
        return calledRobotFile;
    }

    public synchronized void incRunCount() {
        runCount++;
    }

    public int getRunCount() {
        return runCount;
    }

    public synchronized void signalStop() {
        stop = true;
    }

    public synchronized boolean shouldStop() {
        return stop;
    }
}


