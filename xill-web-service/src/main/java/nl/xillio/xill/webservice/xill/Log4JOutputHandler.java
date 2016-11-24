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
package nl.xillio.xill.webservice.xill;

import nl.xillio.xill.api.OutputHandler;
import nl.xillio.xill.api.components.Instruction;
import nl.xillio.xill.api.components.RobotID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

/**
 * Handles robot output and writes it to Log4J.
 *
 * @author Thomas Biesaart
 * @author Geert Konijnendijk
 */
@Component
public class Log4JOutputHandler implements OutputHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void handleLog(RobotID robotID, Level level, String message, Object... parameters) {
        log(
                "log",
                message,
                org.apache.logging.log4j.Level.valueOf(level.name()),
                robotID,
                parameters
        );
    }

    @Override
    public void inspect(Instruction instruction, Throwable e) {
        if (instruction != null) {
            ThreadContext.put("line", Integer.toString(instruction.getLineNumber()));
            log("error", e.getMessage(), org.apache.logging.log4j.Level.ERROR, instruction.getRobotID(), e);
        } else {
            log("error", e.getMessage(), org.apache.logging.log4j.Level.ERROR, null, e);
        }
    }

    private void log(String type, String message, org.apache.logging.log4j.Level level, RobotID robotID, Object... parameters) {
        ThreadContext.put("robot", robotID.getPath().toString());
        ThreadContext.put("type", type);
        LOGGER.log(level, message, parameters);
        ThreadContext.clearMap();
    }
}
