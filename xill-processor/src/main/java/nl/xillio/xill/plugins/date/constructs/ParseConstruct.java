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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.date.BaseDateConstruct;

import java.time.DateTimeException;

/**
 * Returns a Date. If no parameters are passed, now() is used. The default
 * format for string date values is ISO. Optionally a different format can be
 * passed as second parameter.
 *
 * @author Sander
 */
public class ParseConstruct extends BaseDateConstruct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {

        return new ConstructProcessor(this::process,
                new Argument("date", NULL, ATOMIC),
                new Argument("format", NULL, ATOMIC),
                new Argument("locale", fromValue("en-US"), ATOMIC));
    }

    private MetaExpression process(final MetaExpression dateVar, final MetaExpression formatVar, final MetaExpression localeVar) {
        // Process
        Date result;

        if (dateVar.isNull()) {
            result = dateService.now();
        } else {

            try {
                String formatString = formatVar.isNull() ? null : formatVar.getStringValue();
                result = dateService.parseDate(dateVar.getStringValue(), formatString, localeVar.getStringValue());
            } catch (DateTimeException | IllegalArgumentException e) {
                throw new OperationFailedException("parse date", e.getMessage(), "Try to check if 'date' and 'format' are correct.", e);
            }
        }

        return fromValue(result);
    }
}
