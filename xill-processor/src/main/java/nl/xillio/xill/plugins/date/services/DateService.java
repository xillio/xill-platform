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
package nl.xillio.xill.plugins.date.services;


import com.google.inject.ImplementedBy;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.services.XillService;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;

/**
 * Service implementing all functionality needed by the date plugin.
 *
 * @author Geert Konijnendijk
 */
@ImplementedBy(DateServiceImpl.class)
public interface DateService extends XillService {

    /**
     * @return The current date
     */
    Date now();

    /**
     * Create a new {@link Date}
     *
     * @param year      the year.
     * @param month     the month.
     * @param day       the day.
     * @param hour      the hour.
     * @param minute    the minute.
     * @param second    the second.
     * @param nano      the nanosecond.
     * @param zone Timezone to be used for the new date
     * @return {@link Date} from the given parameters
     */
    Date constructDate(int year, int month, int day, int hour, int minute, int second, int nano, ZoneId zone);

    /**
     * Parse a {@link Date} from a String.
     *
     * @param date   The String to parse from.
     * @param format The format String according to
     * @return A new {@link Date}
     */
    Date parseDate(String date, String format);

    /**
     * Adds a number of different time units to a {@link Date}.
     *
     * @param original The orginal date to add to
     * @param toAdd    A map containing {@link ChronoUnit ChronoUnits} as key and the amount of the unit to add as a value.
     * @return The resulting time
     */
    Date add(Date original, Map<ChronoUnit, Long> toAdd);

    /**
     * Change the timezone of a {@link Date}
     *
     * @param original Original date
     * @param newZone  Timezone to chenge to
     * @return A new {@link Date}
     */
    Date changeTimeZone(Date original, ZoneId newZone);

    /**
     * Format a {@link Date} as a String
     *
     * @param date   The date to format
     * @param format Format string according to {@link DateTimeFormatter#ofPattern(String)}
     * @return The formatted date
     */
    String formatDate(Date date, String format);

    /**
     * Format a {@link Date} for a given locale
     *
     * @param date      Date to format
     * @param dateStyle Length of the date, can be null
     * @param timeStyle Length of the time, can be null
     * @param locale    Locale to format for, can be null
     * @return The formatted date
     */
    String formatDateLocalized(Date date, FormatStyle dateStyle, FormatStyle timeStyle, Locale locale);

    /**
     * @param date The date to process
     * @return A map with the names of all supported {@link ChronoField ChronoFields} as keys and their value as values.
     */
    Map<String, Long> getFieldValues(Date date);

    /**
     * Get the timezone of the date
     *
     * @param date Date to get the timezone from
     * @return The timezone of the date
     */
    ZoneId getTimezone(Date date);

    /**
     * Tests if the date is in the future
     *
     * @param date Date to test
     * @return True if the date is in the future
     */
    boolean isInFuture(Date date);

    /**
     * Tests if the date is in the past
     *
     * @param date Date to test
     * @return True if the date is in the past
     */
    boolean isInPast(Date date);

    /**
     * Calculates the difference between two dates
     *
     * @param date1 First comparison date
     * @param date2 Second comparison date
     * @param absolute    whether an absolute value needs to be returned.
     * @return A map containing names of time units as keys (first letter capitlized and in plural) and the amount of this time unit as value. The amount is always positive. When the key has "Total "
     * prepended, the value is not additive.
     */
    Map<String, Number> difference(Date date1, Date date2, boolean absolute);
}
