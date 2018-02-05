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
package nl.xillio.xill.plugins.mongodb.services;

import com.google.inject.Singleton;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.mongodb.services.serializers.MongoIdSerializer;
import org.bson.Document;

import javax.inject.Inject;
import java.util.Map;

/**
 * This class is responsible for the conversion of xill objects to Document objects.
 *
 * @author Thomas Biesaart
 * @author Titus Nachbauer
 */
@Singleton
public class MongoConverter {
    private final MongoIdSerializer mongoIdSerializer;

    @Inject
    public MongoConverter(MongoIdSerializer mongoIdSerializer) {
        this.mongoIdSerializer = mongoIdSerializer;
    }


    public Document parse(MetaExpression expression) {

        if (expression.getType() != ExpressionDataType.OBJECT) {
            throw new IllegalArgumentException("Can only parse OBJECT to a query");
        }

        Map<String, Object> value = MetaExpression.extractValue(expression, mongoIdSerializer);

        return new Document(value);
    }

    public MetaExpression parse(Document document) {
        return MetaExpression.parseObject(document, mongoIdSerializer);
    }
}
