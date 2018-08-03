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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.mongodb.data.MongoObjectId;
import nl.xillio.xill.plugins.mongodb.data.MongoUUID;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.util.UUID;

import static org.testng.Assert.*;

public class MongoSerializerTest extends TestUtils {

    private MongoSerializer serializer = new MongoSerializer(
            new ObjectIdSerializer(),
            new UUIDSerializer(),
            new MongoRegexSerializer(),
            new BinarySerializer());

    @Test
    public void testExtractObjectId() {
        MetaExpression uuidContainer = fromValue("55348611a56c10449ab80a4f");
        uuidContainer.storeMeta(new MongoObjectId("55348611a56c10449ab80a4f"));
        assertEquals(
                serializer.extractValue(uuidContainer),
                new ObjectId("55348611a56c10449ab80a4f")
        );
    }

    @Test
    public void testExtractUUID() {
        MetaExpression uuidContainer = fromValue("2689b00c-2da0-4cb8-8ecd-3e4f42875fd8");
        uuidContainer.storeMeta(new MongoUUID("2689b00c-2da0-4cb8-8ecd-3e4f42875fd8"));

        ByteBuffer buffer = ByteBuffer.wrap(((Binary) serializer.extractValue(uuidContainer)).getData());
        long l1 = buffer.getLong();
        long l2 = buffer.getLong();

        assertEquals(
                new UUID(l1, l2),
                UUID.fromString("2689b00c-2da0-4cb8-8ecd-3e4f42875fd8")
        );
    }

    @Test
    public void testExtractNothing() {
        assertNull(serializer.extractValue(fromValue("not an id")));
    }

    @Test
    public void testParseObjectId() {
        assertTrue(serializer.parseObject(UUID.fromString("2689b00c-2da0-4cb8-8ecd-3e4f42875fd8")).hasMeta(MongoUUID.class));
    }

    @Test
    public void testParseUUID() {
        assertTrue(serializer.parseObject(new ObjectId("55348611a56c10449ab80a4f")).hasMeta(MongoObjectId.class));
    }

    @Test
    public void testParseNothing() {
        assertNull(serializer.parseObject("Not a UUID or an ObjectId"));
    }
}
