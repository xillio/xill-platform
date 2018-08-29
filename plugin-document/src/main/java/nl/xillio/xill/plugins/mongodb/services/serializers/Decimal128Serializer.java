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

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;

public class Decimal128Serializer implements MetaExpressionDeserializer {

    @Override
    public MetaExpression parseObject(Object object) {
        if (object instanceof Decimal128) {
            return fromValue(((Decimal128) object).bigDecimalValue());
        }
        return null;
    }
}
