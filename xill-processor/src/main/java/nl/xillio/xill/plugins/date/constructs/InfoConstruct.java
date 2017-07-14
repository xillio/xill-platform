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

import java.util.LinkedHashMap;

/**
 * Returns detailed info on the specified date.
 *
 * @author Sander
 */
public class InfoConstruct extends BaseDateConstruct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(this::process,
                new Argument("date")
        );
    }

    private MetaExpression process(final MetaExpression dateVar) {
        Date date = getDate(dateVar, "date");

        LinkedHashMap<String, MetaExpression> info = new LinkedHashMap<>();

        // Get ChronoField values
        dateService.getFieldValues(date).forEach((k, v) -> info.put(k, fromValue(v)));

        info.put("timeZone", fromValue(dateService.getTimezone(date).toString()));
        info.put("isInFuture", fromValue(dateService.isInFuture(date)));
        info.put("isInPast", fromValue(dateService.isInPast(date)));

        return fromValue(info);

    }
}
