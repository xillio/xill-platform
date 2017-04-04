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
package nl.xillio.xill.api;

import nl.xillio.xill.api.components.Instruction;
import nl.xillio.xill.api.components.RobotID;

/**
 * This output handler performs no actions. It exists to provide a convenient way to create
 * new output handlers that only consume a specific event.
 *
 * @author Thomas Biesaart
 */
public class DefaultOutputHandler implements OutputHandler {
    @Override
    public void handleLog(RobotID robotID, String level, String message, Object... parameters) {
        // No Op
    }

    @Override
    public void inspect(Instruction instruction, Throwable e) {
        // No Op
    }
}
