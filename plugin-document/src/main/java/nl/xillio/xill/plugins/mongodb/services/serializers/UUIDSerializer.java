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
import nl.xillio.xill.plugins.mongodb.data.MongoUUID;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;

public class UUIDSerializer implements MetaExpressionSerializer, MetaExpressionDeserializer {

    @Override
    public MetaExpression parseObject(Object object) {
        if (!(object instanceof UUID)) {
            return null;
        }
        MetaExpression result = fromValue(object.toString());
        result.storeMeta(new MongoUUID(object.toString()));
        return result;
    }

    @Override
    public Binary extractValue(MetaExpression metaExpression) {
        MongoUUID result = metaExpression.getMeta(MongoUUID.class);
        if (result != null) {
            UUID uuid = result.getUuid();

            byte[] bytes = new byte[16];
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.putLong(uuid.getMostSignificantBits());
            buffer.putLong(uuid.getLeastSignificantBits());

            return new Binary(BsonBinarySubType.UUID_STANDARD, buffer.array());
        }
        return null;
    }
}
