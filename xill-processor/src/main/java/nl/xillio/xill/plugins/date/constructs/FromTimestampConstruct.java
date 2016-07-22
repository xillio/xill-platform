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
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.date.BaseDateConstruct;
import nl.xillio.xill.plugins.date.services.DateService;

/**
 * Creates a date from a Unix timestamp.
 */
public class FromTimestampConstruct extends BaseDateConstruct {
    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(timestampVar -> process(timestampVar, getDateService()),
                new Argument("timestamp", ATOMIC)
        );
    }

    static MetaExpression process(final MetaExpression timestampVar, DateService dateService) {
        Number num = timestampVar.getNumberValue();

        // Check if the timestamp is a number.
        if (Double.isNaN(num.doubleValue())) {
            throw new OperationFailedException("parse timestamp", "\"" + timestampVar.getStringValue() + "\" is not a number.", "Make sure timestamp is a number.");
        }

        return fromValue(dateService.fromTimestamp(num.longValue()));
    }
}
