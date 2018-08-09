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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.api.data.DateFactory;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.mongodb.services.serializers.BinarySerializer;
import nl.xillio.xill.plugins.mongodb.services.serializers.MongoRegexSerializer;
import nl.xillio.xill.plugins.mongodb.services.serializers.ObjectIdSerializer;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static nl.xillio.xill.api.components.ExpressionBuilder.NULL;
import static nl.xillio.xill.api.components.ExpressionBuilder.fromValue;

/**
 * This class is responsible for converting a {@link BsonValue} to a {@link MetaExpression}.
 *
 * @author Thomas Biesaart
 */
@Singleton
public class BsonValueConverter {

    private final DateFactory dateFactory;
    private final ObjectIdSerializer objectIdSerializer;
    private final MongoRegexSerializer mongoRegexSerializer;
    private final BinarySerializer binarySerializer;

    @Inject
    public BsonValueConverter(DateFactory dateFactory, ObjectIdSerializer objectIdSerializer, MongoRegexSerializer mongoRegexSerializer, BinarySerializer binarySerializer) {
        this.dateFactory = dateFactory;
        this.objectIdSerializer = objectIdSerializer;
        this.mongoRegexSerializer = mongoRegexSerializer;
        this.binarySerializer = binarySerializer;
    }

    public MetaExpression convert(BsonValue value) {
        if (value.isArray()) {
            return fromValue(value.asArray().stream().map(this::convert).collect(Collectors.toList()));
        }

        if (value.isDocument()) {
            LinkedHashMap<String, MetaExpression> result = convertDocument(value);
            return fromValue(result);
        }

        if (value.isNull()) {
            return NULL;
        }

        if (value.isBoolean()) {
            return fromValue(value.asBoolean().getValue());
        }

        if (value.isString()) {
            return fromValue(value.asString().getValue());
        }

        if (value.isInt64()) {
            return fromValue(value.asInt64().getValue());
        }

        if (value.isInt32()) {
            return fromValue(value.asInt32().getValue());
        }

        if (value.isDouble()) {
            return fromValue(value.asDouble().getValue());
        }

        if (value.isTimestamp()) {
            Instant instant = Instant.ofEpochSecond(value.asTimestamp().getTime());
            return parseDate(instant);
        }

        if (value.isDateTime()) {
            Instant instant = Instant.ofEpochSecond(value.asDateTime().getValue());
            return parseDate(instant);
        }

        if (value.isObjectId()) {
            MetaExpression objectId = objectIdSerializer.parseObject(value.asObjectId().getValue());
            return objectId == null ? NULL : objectId;
        }

        if (value.isRegularExpression()) {
            MetaExpression regex = mongoRegexSerializer.parseObject(Pattern.compile(value.asRegularExpression().getPattern()));
            return regex == null ? NULL : regex;
        }

        if (value.isBinary()) {
            MetaExpression binary = binarySerializer.parseObject(value.asBinary());
            return binary == null ? NULL : binary;
        }

        throw new RobotRuntimeException("No conversion codex found for bson type " + value.getClass().getSimpleName());
    }

    private LinkedHashMap<String, MetaExpression> convertDocument(BsonValue value) {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();
        BsonDocument doc = value.asDocument();
        doc.keySet().forEach(key -> result.put(key, convert(doc.get(key))));
        return result;
    }

    private MetaExpression parseDate(Instant instant) {
        Date date = dateFactory.from(instant);
        MetaExpression result = fromValue(date.toString());
        result.storeMeta(date);
        return result;
    }
}
