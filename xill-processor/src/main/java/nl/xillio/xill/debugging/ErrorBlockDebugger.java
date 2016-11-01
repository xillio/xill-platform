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
package nl.xillio.xill.debugging;

import me.biesaart.utils.Log;
import nl.xillio.xill.api.Debugger;
import org.slf4j.Logger;

import java.util.NoSuchElementException;

/**
 * This class contains all information and controls required for debugging.
 *
 * @author Thomas Biesaart
 */
public class ErrorBlockDebugger extends DelegateDebugger {

    private static final Logger LOGGER = Log.get();
    private Throwable error;
    private nl.xillio.xill.api.components.Instruction erroredInstruction;

    public ErrorBlockDebugger(Debugger parentDebugger) {
        super(parentDebugger);
    }

    @Override
    public void handle(Throwable e) {
        // Multiple exceptions can occur on one line of code, the do block is exited after the line is completed
        // The first exception probably caused the following exceptions, they should be suppressed
        if (this.error == null) {
            this.error = e;
            if (!getStackTrace().isEmpty()) {
                erroredInstruction = getStackTrace().get(getStackTrace().size() - 1);
            }
        } else {
            this.error.addSuppressed(e);
        }
        LOGGER.debug("Caught exception in error handler", e);
    }

    @Override
    public boolean shouldStop() {
        return super.shouldStop() || hasError();
    }

    public boolean hasError() {
        return error != null;
    }

    public Throwable getError() {
        if (hasError()) {
            return this.error;
        }
        throw new NoSuchElementException("No error was caught");
    }

    public nl.xillio.xill.api.components.Instruction getErroredInstruction() {
        return erroredInstruction;
    }
}
