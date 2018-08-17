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
package nl.xillio.xill.plugins.mongodb.services.serializers;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.MetaExpressionDeserializer;
import org.bson.types.Decimal128;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;

public class Decimal128Serializer implements MetaExpressionDeserializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Decimal128Serializer.class);

    @Override
    public MetaExpression parseObject(Object object) {
        if (object instanceof Decimal128) {
            if (object.toString().contains(".")) {
                return fromValue(((Decimal128) object).bigDecimalValue().doubleValue());
            } else {
                try {
                    return fromValue(((Decimal128) object).bigDecimalValue().longValueExact());
                } catch (ArithmeticException e) {
                    LOGGER.warn("Failed to parse Decimal128 as a long", e);
                    return fromValue(((Decimal128) object).bigDecimalValue());
                }
            }
        }
        return null;
    }
}
