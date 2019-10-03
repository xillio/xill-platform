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
import com.google.inject.name.Named;
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

import java.time.temporal.Temporal;
import java.util.function.Function;

/**
 * Base Construct to transform a {@link MetaExpression} of a date as created by the {@code Date} plugin into a suitable date type
 * to be consumed by the JDBC Layer.
 * <p>
 * This construct depends indirectly on the {@link TemporalConversionService} where the actual RDBMS platform data types
 * to java JDBC types correspondence is implemented.
 * So to obtain a conforming implementation for a given platform, the constructs can be left as-is, while the
 * {@link TemporalConversionService} methods should be overridden to provide the desired mapping.
 *
 * @author Andrea Parrilli
 */
public abstract class BaseTemporalConversionConstruct extends Construct {
    protected final String docRoot;
    private final Function<Date, Temporal> toTargetType;

    protected BaseTemporalConversionConstruct(Function<Date, Temporal> toTargetType, String docRoot) {
        this.toTargetType = toTargetType;
        this.docRoot = docRoot;
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
            result.storeMeta(new TemporalMetadataExpression(toTargetType.apply(source.getMeta(Date.class))));
            return result;
        } else {
            throw new InvalidUserInputException("Can only convert values made by the Date plugin", source.getStringValue(), "Date.now()");
        }
    }
}
