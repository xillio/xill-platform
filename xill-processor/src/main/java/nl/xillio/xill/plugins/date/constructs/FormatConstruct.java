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
import nl.xillio.xill.plugins.date.services.DateService;

import java.time.DateTimeException;

/**
 * converts the date to a string using the provided format.
 * if no format is given the pattern "yyyy-MM-dd HH:mm:ss" is used.
 *
 * @author Sander
 */
public class FormatConstruct extends BaseDateConstruct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {

        return new ConstructProcessor((dateVar, formatVar, localeVar) -> process(dateVar, formatVar, localeVar, getDateService()),
                new Argument("date"),
                new Argument("format", NULL),
                new Argument("locale" , fromValue("en-US"), ATOMIC));
    }

    static MetaExpression process(final MetaExpression dateVar,
                                  final MetaExpression formatVar,
                                  final MetaExpression localeVar,
                                  DateService dateService) {

        Date date = getDate(dateVar, "date");

        try {
            String formatString = formatVar.isNull() ? null : formatVar.getStringValue();
            return fromValue(dateService.parseDate(dateVar.getStringValue(), formatString, localeVar.getStringValue()));
        } catch (DateTimeException | IllegalArgumentException e) {
            throw new OperationFailedException("format a date", e.getMessage(), e);
        }
    }
}
