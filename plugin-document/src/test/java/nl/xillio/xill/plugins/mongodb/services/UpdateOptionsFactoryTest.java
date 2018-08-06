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

import com.mongodb.client.model.UpdateOptions;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.mongodb.services.serializers.*;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class UpdateOptionsFactoryTest extends TestUtils {
    @Test
    public void testBuildOptions() {
        MongoConverter mongoConverter = new MongoConverter(new MongoSerializer(
                new ObjectIdSerializer(),
                new UUIDSerializer(),
                new MongoRegexSerializer(),
                new BinarySerializer()));
        UpdateOptionsFactory UpdateOptionsFactory = new UpdateOptionsFactory(mongoConverter);
        LinkedHashMap<String, MetaExpression> object = new LinkedHashMap<>();

        UpdateOptions options = UpdateOptionsFactory.build(fromValue(object));

        assertFalse(options.isUpsert());
        object.put("upsert", TRUE);
        options = UpdateOptionsFactory.build(fromValue(object));
        assertTrue(options.isUpsert());
    }
}
