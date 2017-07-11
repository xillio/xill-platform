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
import java.util.Map;

/**
 * Returns the difference between two dates. By default the function will return
 * the absolute difference. Optionally you can set 'absolute' to false to get
 * the relative difference.
 *
 * @author Sander
 */
public class DiffConstruct extends BaseDateConstruct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(this::process,
                new Argument("date"),
                new Argument("other"),
                new Argument("absolute", TRUE)
        );
    }

    private MetaExpression process(final MetaExpression dateVar, final MetaExpression otherVar, final MetaExpression absolute) {
        Date date = getDate(dateVar, "date");
        Date other = getDate(otherVar, "other");

        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();

        Map<String, Number> diff = dateService.difference(date, other, absolute.getBooleanValue());

        diff.forEach((k, v) -> result.put(k, fromValue(v)));

        return fromValue(result);
    }
}
