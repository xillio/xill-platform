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
import nl.xillio.xill.plugins.date.BaseDateConstruct;
import org.slf4j.Logger;

import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Modifies the provided date with the specified changes from a list.
 *
 * @author Sander
 */
public class ChangeConstruct extends BaseDateConstruct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor((date, change) -> process(context.getRootLogger(), date, change),
                new Argument("date"),
                new Argument("change", OBJECT)
        );
    }

    @SuppressWarnings("squid:S1166")
    // IllegalArgumentException is an internal robot exception, which is handled correctly
    private MetaExpression process(final Logger logger, final MetaExpression dateVar, final MetaExpression changeVar) {
        Date date = getDate(dateVar, "date");

        // First we need the zone
        ZoneId zone = null;
        @SuppressWarnings("unchecked")
        Map<String, MetaExpression> map = changeVar.getValue();
        if (map.containsKey("zone")) {
            zone = ZoneId.of(map.get("zone").getStringValue());
        } else if (map.containsKey("timezone")) {
            zone = ZoneId.of(map.get("timezone").getStringValue());
        }

        Date newDate = date;
        if (zone != null) {
            // Change the timezone to the new one
            newDate = dateService.changeTimeZone(date, zone);
        }

        Map<ChronoUnit, Long> changes = new HashMap<>();

        // Convert changes
        for (Entry<String, MetaExpression> entry : map.entrySet()) {
            try {
                ChronoUnit unit = ChronoUnit.valueOf(entry.getKey().toUpperCase());
                long value = entry.getValue().getNumberValue().longValue();
                changes.put(unit, value);
            } catch (IllegalArgumentException e) {
                String lower = entry.getKey().toLowerCase();
                if (!("zone".equals(lower) || "timezone".equals(lower))) {
                    logger.warn("`" + entry.getKey() + "` is not a valid date change operation.");
                }
            }
        }

        // Add changes
        newDate = dateService.add(newDate, changes);

        return fromValue(newDate);
    }
}
