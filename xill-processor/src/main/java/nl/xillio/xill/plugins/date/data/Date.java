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
package nl.xillio.xill.plugins.date.data;


import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * This class represents a date MetadataExpression
 * </p>
 *
 * @author Thomas Biesaart
 * @since 7-8-2015
 */
public class Date implements nl.xillio.xill.api.data.Date {
    private final ZonedDateTime date;

    public Date(ZonedDateTime date) {
        if (date == null) {
            throw new NullPointerException();
        }
        this.date = date;
    }

    /**
     * Returns a ZonedDateTime
     *
     * @return the date
     */
    public ZonedDateTime getZoned() {
        return date;
    }

    @Override
    public String toString() {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(getZoned());
    }
}
