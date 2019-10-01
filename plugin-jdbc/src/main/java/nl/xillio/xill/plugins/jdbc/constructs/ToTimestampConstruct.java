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
package nl.xillio.xill.plugins.jdbc.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.plugins.jdbc.data.TemporalMetadataExpression;
import nl.xillio.xill.plugins.jdbc.services.TemporalConversionService;

public class ToTimestampConstruct extends Construct {
    private final TemporalConversionService temporalConversionService;

    @Inject
    public ToTimestampConstruct(TemporalConversionService temporalConversionService) {
        this.temporalConversionService = temporalConversionService;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                this::convert,
                new Argument("source", ExpressionDataType.ATOMIC)
        );
    }

    private MetaExpression convert(MetaExpression source) {
        if (source.hasMeta(Date.class)) {
            MetaExpression result = fromValue(source.getStringValue());
            result.storeMeta(new TemporalMetadataExpression(temporalConversionService.toTimestamp(source.getMeta(Date.class))));
            return result;
        } else {
            throw new InvalidUserInputException("Can only convert values made by the Date plugin", source.getStringValue(), "Date.now()");
        }
    }
}
