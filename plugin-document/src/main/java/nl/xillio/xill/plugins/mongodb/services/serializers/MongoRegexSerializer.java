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
import nl.xillio.xill.api.components.MetaExpressionSerializer;
import nl.xillio.xill.plugins.mongodb.data.MongoRegex;

import java.util.regex.Pattern;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;

/**
 * Provides a deserializer for a MongoRegex.
 */
public class MongoRegexSerializer implements MetaExpressionSerializer, MetaExpressionDeserializer {

    @Override
    public MetaExpression parseObject(Object object) {
        if (!(object instanceof Pattern)) {
            return null;
        }
        MetaExpression result = fromValue(object.toString());
        result.storeMeta(new MongoRegex(object.toString()));
        return result;
    }

    @Override
    public Object extractValue(MetaExpression metaExpression) {
        MongoRegex result = metaExpression.getMeta(MongoRegex.class);
        if (result != null) {
            return result.getPattern();
        }
        return null;
    }
}
