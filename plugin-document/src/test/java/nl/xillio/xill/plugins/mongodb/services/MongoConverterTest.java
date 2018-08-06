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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.mongodb.data.MongoObjectId;
import nl.xillio.xill.plugins.mongodb.services.serializers.*;
import nl.xillio.xill.services.json.JacksonParser;
import nl.xillio.xill.services.json.JsonException;
import nl.xillio.xill.services.json.JsonParser;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MongoConverterTest extends TestUtils {
    private JsonParser parser = new JacksonParser(false);

    @Test
    public void testParseEmptyMetaExpression() throws JsonException {
        MongoConverter mongoConverter = new MongoConverter(null);
        MetaExpression expression = parse("{}");
        Document document = mongoConverter.parse(expression);

        assertEquals(document.size(), 0);
    }

    @Test
    public void testParseMetaExpression() throws JsonException {
        MongoConverter mongoConverter = new MongoConverter(new MongoSerializer(
                new ObjectIdSerializer(),
                new UUIDSerializer(),
                new MongoRegexSerializer(),
                new BinarySerializer()));
        MetaExpression expression = parse("{ \"$set\": { \"test\" : 2 } }");
        Document document = mongoConverter.parse(expression);

        assertEquals(document.size(), 1);
        assertTrue(document.containsKey("$set"));
        assertEquals(document.get("$set").toString(), "{test=2}");
    }

    @Test
    public void testParseMetaExpressionWithObjectId() {
        String id = "567a6f35cfa90423ac88865e";
        MongoConverter mongoConverter = new MongoConverter(new MongoSerializer(
                new ObjectIdSerializer(),
                new UUIDSerializer(),
                new MongoRegexSerializer(),
                new BinarySerializer()));
        LinkedHashMap<String, MetaExpression> object = new LinkedHashMap<>();
        MetaExpression objectId = fromValue(id);
        objectId.storeMeta(new MongoObjectId(id));
        object.put("_id", objectId);
        Document document = mongoConverter.parse(fromValue(object));

        assertEquals(document.size(), 1);
        assertEquals(document.getObjectId("_id"), new ObjectId(id));
    }

    @Test
    public void testParseBsonDocument() throws JsonException {
        MongoConverter mongoConverter = new MongoConverter(null);
        Document document = new Document("$set", new Document("test", 2));
        MetaExpression expression = mongoConverter.parse(document);

        assertEquals(expression.toString(parser), "{\"$set\":{\"test\":2}}");
    }

    private MetaExpression parse(String json) throws JsonException {
        Map<?, ?> value = parser.fromJson(json, Map.class);
        return MetaExpression.parseObject(value);
    }
}
