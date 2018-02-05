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
package nl.xillio.xill.plugins.mongodb.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.mongodb.data.MongoObjectId;
import org.bson.types.ObjectId;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ObjectIdConstructTest extends TestUtils {
    @Test
    public void testConstruct() {
        MetaExpression result = process(new ObjectIdConstruct(), fromValue("5a74557825a3e5291c11b0d9"));
        assertEquals(
                result.getStringValue(),
                "5a74557825a3e5291c11b0d9"
        );
        assertEquals(
                result.getMeta(MongoObjectId.class).getObjectId(),
                new ObjectId("5a74557825a3e5291c11b0d9")
        );
    }

    @Test
    public void testGenerateObjectId() {
        MetaExpression result = process(new ObjectIdConstruct(), NULL);
        ObjectId objectId = result.getMeta(MongoObjectId.class).getObjectId();

        assertEquals(
                result.getStringValue(),
                objectId.toString()
        );
    }
}
