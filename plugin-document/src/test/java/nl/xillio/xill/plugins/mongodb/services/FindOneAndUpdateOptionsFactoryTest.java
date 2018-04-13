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

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.mongodb.services.serializers.BinarySerializer;
import nl.xillio.xill.plugins.mongodb.services.serializers.MongoIdSerializer;
import nl.xillio.xill.plugins.mongodb.services.serializers.ObjectIdSerializer;
import nl.xillio.xill.plugins.mongodb.services.serializers.UUIDSerializer;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

public class FindOneAndUpdateOptionsFactoryTest extends TestUtils {
    @Test
    public void testBuildOptions() {
        MongoConverter mongoConverter = new MongoConverter(new MongoIdSerializer(new ObjectIdSerializer(), new UUIDSerializer(), new BinarySerializer()));
        FindOneAndUpdateOptionsFactory findOneAndUpdateOptionsFactory = new FindOneAndUpdateOptionsFactory(mongoConverter);
        LinkedHashMap<String, MetaExpression> object = new LinkedHashMap<>();

        FindOneAndUpdateOptions options = findOneAndUpdateOptionsFactory.build(fromValue(object));
        LinkedHashMap<String, MetaExpression> projection = new LinkedHashMap<>();

        assertNull(options.getProjection());
        projection.put("_id", FALSE);
        projection.put("target", TRUE);
        object.put("projection", fromValue(projection));
        options = findOneAndUpdateOptionsFactory.build(fromValue(object));
        assertNotNull(options.getProjection());
        assertEquals(MetaExpression.parseObject(options.getProjection()), object.get("projection"));

        assertNull(options.getSort());
        LinkedHashMap<String, MetaExpression> sort = new LinkedHashMap<>();
        sort.put("target.timestamp", fromValue(-1));
        object.put("sort", fromValue(sort));
        options = findOneAndUpdateOptionsFactory.build(fromValue(object));
        assertNotNull(options.getSort());
        assertEquals(MetaExpression.parseObject(options.getSort()), object.get("sort"));

        assertFalse(options.isUpsert());
        object.put("upsert", TRUE);
        options = findOneAndUpdateOptionsFactory.build(fromValue(object));
        assertTrue(options.isUpsert());

        assertEquals(options.getReturnDocument(), ReturnDocument.BEFORE);
        object.put("returnNew", TRUE);
        options = findOneAndUpdateOptionsFactory.build(fromValue(object));
        assertEquals(options.getReturnDocument(), ReturnDocument.AFTER);

        assertEquals(options.getMaxTime(TimeUnit.MILLISECONDS), 0);
        object.put("maxTime", fromValue(2000));
        options = findOneAndUpdateOptionsFactory.build(fromValue(object));
        assertNotNull(options.getMaxTime(TimeUnit.MILLISECONDS));
        assertEquals(MetaExpression.parseObject(options.getMaxTime(TimeUnit.MILLISECONDS)), object.get("maxTime"));
    }
}
