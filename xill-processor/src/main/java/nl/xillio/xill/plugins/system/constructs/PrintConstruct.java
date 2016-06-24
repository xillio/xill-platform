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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import org.slf4j.Logger;

/**
 * Prints the provided text to the log. Optional the log level can be set:
 * debug, info (default), warning, error.
 */
public class PrintConstruct extends Construct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (text, level) -> process(text, level, context.getRootLogger()),
                new Argument("text", fromValue("")),
                new Argument("logLevel", fromValue("info"), ATOMIC));
    }

    static MetaExpression process(final MetaExpression textVar, final MetaExpression logLevel, final Logger robotLog) {
        String level = logLevel.getStringValue();

        String text = textVar.getStringValue();

        if ("debug".equalsIgnoreCase(level)) {
            robotLog.debug(text);
        } else if (level.toLowerCase().startsWith("warn")) {
            robotLog.warn(text);
        } else if ("error".equalsIgnoreCase(level)) {
            robotLog.error(text);
        } else {
            robotLog.info(text);
        }

        return NULL;
    }
}
