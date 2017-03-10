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

import nl.xillio.xill.api.Debugger;
import xill.RobotLoader;

/**
 * Controls the {@link RunBulkExpression} threads.
 */
class RunBulkControl {
    private int runCount = 0;
    private boolean stop = false;
    private final Debugger debugger;
    private final String calledRobotFqn;
    private final RobotLoader loader;

    public RunBulkControl(final Debugger debugger, final String calledRobotFqn, final RobotLoader loader) {
        this.debugger = debugger;
        this.calledRobotFqn = calledRobotFqn;
        this.loader = loader;
    }

    public Debugger getDebugger() {
        return debugger;
    }

    public String getCalledRobotFqn() {
        return calledRobotFqn;
    }

    public RobotLoader getLoader() {
        return loader;
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


