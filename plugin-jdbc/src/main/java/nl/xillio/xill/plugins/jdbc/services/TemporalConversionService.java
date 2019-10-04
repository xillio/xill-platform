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
package nl.xillio.xill.plugins.jdbc.services;

import nl.xillio.xill.api.data.Date;

import java.time.temporal.Temporal;

/**
 * A service to convert {@link Date}s to different {@link Temporal} types by their usual ANSI SQL name.
 *
 * @author Andrea Parrilli
 */
public class TemporalConversionService {
    /**
     * Convert to {@code DATE}.
     *
     * @param date the date to convert
     * @return the date as a Java type compatible with SQL {@code DATE}
     */
    public Temporal toDate(Date date) {
        return date.getZoned().toLocalDate();
    }

    /**
     * Convert to {@code TIMESTAMP}.
     *
     * @param date the date to convert
     * @return the date as a Java type compatible with SQL {@code TIMESTAMP}
     */
    public Temporal toTimestamp(Date date) {
        return date.getZoned().toLocalDateTime();
    }
}
