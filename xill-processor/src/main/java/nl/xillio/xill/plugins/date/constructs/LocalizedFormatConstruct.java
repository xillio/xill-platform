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
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.plugins.date.BaseDateConstruct;

import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Converts the date to a string using the provided Language tag. If no tag is given the pattern "yyyy-MM-dd HH:mm:ss" is used.
 * If the parameters 'timeStyle' and 'dateStyle' are both null, it will use the FormatStyle MEDIUM.
 * If only 'timeStyle' is null, it will return the date. if only 'dateStyle' is null. it will return the time.
 * if both are not null it will return the date and time;
 * <p>
 * timeStyle and dateStyle have to be 'full','long','medium' or 'short'.
 *
 * @author Sander
 */
public class LocalizedFormatConstruct extends BaseDateConstruct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {

        return new ConstructProcessor(this::process,
                new Argument("date"),
                new Argument("languageTag", NULL),
                new Argument("dateStyle", NULL),
                new Argument("timeStyle", NULL)
        );
    }

    private MetaExpression process(final MetaExpression dateVar, final MetaExpression localeVar, final MetaExpression languageTag, final MetaExpression timeStyleVar) {
        Date date = getDate(dateVar, "date");
        FormatStyle dateStyle;
        FormatStyle timeStyle;

        // no styles are given so we use medium
        if (languageTag.isNull()) {
            dateStyle = FormatStyle.MEDIUM;
        } else {
            try {
                dateStyle = FormatStyle.valueOf(languageTag.getStringValue().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidUserInputException("Invalid dateStyle value.", languageTag.getStringValue(), "A one of following values: 'full','long','medium' or 'short'", "use System, Date;\n" +
                        "var date = Date.now();\n" +
                        "System.print(Date.localizedFormat(date, \"en-GB\",\"full\",\"full\"));", e);
            }
        }

        if (timeStyleVar.isNull()) {
            timeStyle = FormatStyle.MEDIUM;
        } else {
            try {
                timeStyle = FormatStyle.valueOf(timeStyleVar.getStringValue().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidUserInputException("Invalid timeStyle value.", timeStyleVar.getStringValue(), "A one of following values: 'full','long','medium' or 'short'", "use System, Date;\n" +
                        "var date = Date.now();\n" +
                        "System.print(Date.localizedFormat(date, \"en-GB\",\"full\",\"full\"));", e);
            }
        }

        Locale locale = localeVar.isNull() ? null : Locale.forLanguageTag(localeVar.getStringValue());
        return fromValue(dateService.formatDateLocalized(date, dateStyle, timeStyle, locale));
    }
}
