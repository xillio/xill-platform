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
package nl.xillio.xill.plugins.date.constructs;

import me.biesaart.utils.Log;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.date.BaseDateConstruct;
import nl.xillio.xill.plugins.date.services.DateService;
import org.slf4j.Logger;

import java.time.DateTimeException;

/**
 * Returns a Date. If no parameters are passed, now() is used. The default
 * format for string date values is ISO. Optionally a different format can be
 * passed as second parameter.
 *
 * @author Sander
 */
public class ParseConstruct extends BaseDateConstruct {

    private static final Logger log = Log.get();

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {

        return new ConstructProcessor((dateVar, formatVar) -> process(dateVar, formatVar, getDateService()), new Argument("date", NULL, ATOMIC),
                new Argument("format", NULL, ATOMIC));
    }

    static MetaExpression process(final MetaExpression dateVar, final MetaExpression formatVar, DateService dateService) {
        // Process
        Date result = null;

        if (dateVar.isNull()) {
            result = dateService.now();
        } else {

            try {
                String formatString = formatVar.isNull() ? null : formatVar.getStringValue();
                result = dateService.parseDate(dateVar.getStringValue(), formatString);
            } catch (DateTimeException | IllegalArgumentException e) {
                log.error("Exception while parsing date", e);
                throw new OperationFailedException("parse date", e.getMessage(), "Try to check if 'date' and 'format' are correct.", e);
            }
        }

        return fromValue(result);
    }
}
