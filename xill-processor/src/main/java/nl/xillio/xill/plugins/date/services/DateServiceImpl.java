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

import com.google.common.base.CaseFormat;
import com.google.inject.Singleton;
import me.biesaart.utils.Log;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.api.data.DateFactory;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.Map.Entry;

/**
 * Implementation providing {@link DateService} functions.
 *
 * @author Sander Visser
 * @author Geert Konijnendijk
 */
@Singleton
public class DateServiceImpl implements DateService, DateFactory {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger LOGGER = Log.get();

    @Override
    public Date now() {
        return new nl.xillio.xill.plugins.date.data.Date(ZonedDateTime.now());
    }

    @Override
    public Date constructDate(int year, int month, int day, int hour, int minute, int second, int nano, ZoneId zone) {
        return new nl.xillio.xill.plugins.date.data.Date(ZonedDateTime.of(year, month, day, hour, minute, second, nano, zone));
    }

    @SuppressWarnings("squid:S1166")
        // DateTimeException is handled correctly
    ZonedDateTime getValueOrDefaultZDT(TemporalAccessor parsed) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime _default = ZonedDateTime.of(now.toLocalDate(), LocalTime.MIN, now.getZone());

        ChronoField[] parameters = {ChronoField.YEAR,
                ChronoField.MONTH_OF_YEAR,
                ChronoField.DAY_OF_MONTH,
                ChronoField.HOUR_OF_DAY,
                ChronoField.MINUTE_OF_HOUR,
                ChronoField.SECOND_OF_MINUTE,
                ChronoField.NANO_OF_SECOND};
        int[] p = Arrays.stream(parameters).mapToInt(cf -> parsed.isSupported(cf) ? parsed.get(cf) : _default.get(cf)).toArray();

        ZoneId zone;
        try {
            zone = ZoneId.from(parsed);
        } catch (DateTimeException dte) {
            zone = ZoneId.systemDefault();
        }

        return ZonedDateTime.of(p[0], p[1], p[2], p[3], p[4], p[5], p[6], zone);
    }

    @Override
    public Date parseDate(String date, String format) {
        DateTimeFormatter formatter = createDateTimeFormatter(format);
        TemporalAccessor parsed = formatter.parse(date);
        return new nl.xillio.xill.plugins.date.data.Date(getValueOrDefaultZDT(parsed));
    }

    private DateTimeFormatter createDateTimeFormatter(String format) {
        return format != null ? DateTimeFormatter.ofPattern(format) : DEFAULT_FORMATTER;
    }

    @Override
    public Date add(Date original, Map<ChronoUnit, Long> toAdd) {
        ZonedDateTime value = original.getZoned();
        for (Entry<ChronoUnit, Long> entry : toAdd.entrySet()) {
            value = value.plus(entry.getValue(), entry.getKey());
        }
        return new nl.xillio.xill.plugins.date.data.Date(value);
    }

    @Override
    public Date changeTimeZone(Date original, ZoneId newZone) {
        return new nl.xillio.xill.plugins.date.data.Date(ZonedDateTime.from(original.getZoned().withZoneSameInstant(newZone)));
    }

    @Override
    public String formatDate(Date date, String format) {
        DateTimeFormatter formatter = createDateTimeFormatter(format);
        return formatter.format(date.getZoned());
    }

    @Override
    public String formatDateLocalized(Date date, FormatStyle dateStyle, FormatStyle timeStyle, Locale locale) {
        DateTimeFormatter formatter;
        if (locale == null) {
            formatter = DEFAULT_FORMATTER;
        } else if (dateStyle == null) {
            if (timeStyle == null) {
                throw new IllegalArgumentException("No dateStyle or timeStyle was provided");
            }
            formatter = DateTimeFormatter.ofLocalizedTime(timeStyle).withLocale(locale);
        } else {
            formatter = timeStyle == null ? DateTimeFormatter.ofLocalizedDate(dateStyle).withLocale(locale)
                    : DateTimeFormatter.ofLocalizedDateTime(dateStyle, timeStyle).withLocale(locale);
        }

        return formatter.format(date.getZoned());
    }

    @Override
    public Map<String, Long> getFieldValues(Date date) {
        LinkedHashMap<String, Long> fields = new LinkedHashMap<>();
        for (ChronoField field : ChronoField.values()) {
            if (date.getZoned().isSupported(field)) {
                fields.put(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, field.toString()), date.getZoned().getLong(field));
            }
        }
        return fields;
    }

    @Override
    public ZoneId getTimezone(Date date) {
        return date.getZoned().getZone();
    }

    @Override
    public boolean isInFuture(Date date) {
        return date.getZoned().isAfter(now().getZoned());
    }

    @Override
    public boolean isInPast(Date date) {
        return date.getZoned().isBefore(now().getZoned());
    }

    @Override
    public Map<String, Double> difference(Date date1, Date date2, boolean absolute) {
        // Calculate difference and convert to seconds
        long nanoDifference = date1.getZoned().until(date2.getZoned(), ChronoUnit.NANOS);
        if (absolute) {
            nanoDifference = Math.abs(nanoDifference);
        }
        BigDecimal difference = new BigDecimal(nanoDifference).multiply(TimeUnits.NANOS.getNumSeconds());
        // Calculate the totals
        Map<String, Double> diff = new LinkedHashMap<>();
        for (TimeUnits t : TimeUnits.values()) {
            diff.put(String.format("total%s", t.getPascalName()), difference.divide(t.getNumSeconds(), RoundingMode.HALF_UP).doubleValue());
        }
        // Calculate the additive differences by going through the TimeUnits in reverse order and
        for (int i = TimeUnits.values().length - 1; i >= 0; i--) {
            TimeUnits unit = TimeUnits.values()[i];
            BigDecimal[] division = difference.divideAndRemainder(unit.getNumSeconds());
            diff.put(unit.getCamelName(), Math.floor(division[0].doubleValue()));
            difference = division[1];
        }
        return diff;
    }

    @Override
    public Date from(Instant instant) {
        long milli = instant.toEpochMilli();

        return new nl.xillio.xill.plugins.date.data.Date(
                ZonedDateTime.from(Instant.ofEpochMilli(milli).atZone(ZoneId.systemDefault()))
        );
    }

    /**
     * Represents different kinds of time units, containing their name and the amount of nanoseconds they contain.
     * <p>
     * The units should be listed in growing order of length.
     *
     * @author Geert Konijnendijk
     */
    private enum TimeUnits {

        // @formatter:off
        NANOS("1E-9"),
        MICROS("1E-6"),
        MILLIS("1E-3"),
        SECONDS("1"),
        MINUTES("60"),
        HOURS("3600"),
        HALF_DAYS("43200"),
        DAYS("86400"),
        WEEKS("604800"),
        MONTHS("2629746"),
        YEARS("31556952"),
        DECADES("31556952E1"),
        CENTURIES("31556952E2"),
        MILLENNIA("31556952E3"),
        ERAS("31556952E9");
        // @formatter:on

        private final BigDecimal numSeconds;
        private final String camelName;
        private final String pascalName;

        /**
         * @param numSeconds Number of seconds that fit into one unit of this kind in {@link BigDecimal} String representation
         */
        TimeUnits(String numSeconds) {
            this.numSeconds = new BigDecimal(numSeconds);
            this.camelName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this.name());
            this.pascalName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.name());
        }

        public BigDecimal getNumSeconds() {
            return numSeconds;
        }

        public String getCamelName() {
            return camelName;
        }

        public String getPascalName() {
            return pascalName;
        }
    }
}
