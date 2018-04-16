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

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class MongoSerializer implements MetaExpressionSerializer, MetaExpressionDeserializer {
    private final List<MetaExpressionDeserializer> deserializers = new ArrayList<>();
    private final List<MetaExpressionSerializer> serializers = new ArrayList<>();

    @Inject
    public MongoSerializer(ObjectIdSerializer objectIdSerializer, UUIDSerializer uuidSerializer, BinarySerializer binarySerializer) {
        deserializers.add(objectIdSerializer);
        serializers.add(objectIdSerializer);
        deserializers.add(uuidSerializer);
        serializers.add(uuidSerializer);
        deserializers.add(binarySerializer);
        serializers.add(binarySerializer);
    }

    @Override
    public MetaExpression parseObject(Object object) {
        for (MetaExpressionDeserializer deserializer : deserializers) {
            MetaExpression result = deserializer.parseObject(object);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Object extractValue(MetaExpression metaExpression) {
        for (MetaExpressionSerializer serializer : serializers) {
            Object result = serializer.extractValue(metaExpression);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
