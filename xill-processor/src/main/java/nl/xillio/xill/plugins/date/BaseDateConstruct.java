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
package nl.xillio.xill.plugins.date;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.plugins.date.services.DateService;

import java.time.ZonedDateTime;

/**
 * This class contains some utility for the constructs to use
 */
public abstract class BaseDateConstruct extends Construct {

    /**
     * Service used by all extending classes
     */
    protected DateService dateService;

    /**
     * Get the date from a variable
     *
     * @param dateVar The expression
     * @param name    The name of the parameter
     * @return        The date.
     */
    protected static Date getDate(final MetaExpression dateVar, final String name) {
        Date date = dateVar.getMeta(Date.class);

        if (date == null) {
            throw new InvalidUserInputException("Expected a date variable.", dateVar.getStringValue(), "Create a date using either Date.parse() or Date.of() or Date.now().");
        }

        return date;
    }

    /**
     * Get the current date and time.
     *
     * @return the current {@link ZonedDateTime}
     */
    protected static ZonedDateTime now() {
        return ZonedDateTime.now();
    }

    /**
     * Create a {@link MetaExpression} from {@link ZonedDateTime}
     *
     * @param date    The provided date object from to retrieve the value.
     * @return        The date value.
     */
    protected static MetaExpression fromValue(final Date date) {
        MetaExpression value = fromValue(date.toString());
        value.storeMeta(date);
        return value;
    }

    @Inject
    public void setDateService(DateService dateService) {
        this.dateService = dateService;
    }
}
