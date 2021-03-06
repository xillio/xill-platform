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
package nl.xillio.xill.plugins.mongodb.data;

import org.bson.types.ObjectId;
import org.testng.annotations.Test;

import static org.testng.Assert.assertSame;

/**
 * Tests for {@link MongoObjectId}
 *
 * @author Geert Konijnendijk
 */
public class MongoObjectIdTest {

    /**
     * Test {@link MongoObjectId#copy()}
     */
    @Test
    public void testCopy() {
        MongoObjectId mongoObjectId = new MongoObjectId(new ObjectId());

        // Run
        MongoObjectId copy = mongoObjectId.copy();

        // Assert

        // An ObjectID is immutable, so should not be cloned
        assertSame(mongoObjectId.getObjectId(), copy.getObjectId());
    }

}
