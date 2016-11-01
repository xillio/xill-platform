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
package nl.xillio.xill.plugins.excel.datastructures;

import nl.xillio.xill.api.data.Date;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Implementation of {@link nl.xillio.xill.api.data.Date} to use the Xill Date Plugin
 * with the Excel Plugin
 *
 * @author Daan Knoope
 */
public class DateImpl implements Date {

    java.util.Date date;

    public DateImpl(java.util.Date date) {
        this.date = date;
    }

    @Override
    public ZonedDateTime getZoned() {
        return ZonedDateTime.ofInstant(this.date.toInstant(), ZoneId.systemDefault());
    }

    @Override
    public Date copy() {
        return new DateImpl(java.util.Date.from(date.toInstant()));
    }
}
