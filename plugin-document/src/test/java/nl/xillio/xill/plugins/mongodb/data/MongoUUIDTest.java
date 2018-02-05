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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;

public class MongoUUIDTest {

    @Test
    public void testCopy() {
        MongoUUID id = new MongoUUID("e98a7da1-fab2-4b29-9cc4-24a7b5b3d0d4");
        MongoUUID copy = id.copy();
        assertEquals(id.getUuid(), copy.getUuid());
        assertNotSame(id, copy);
    }

    @Test
    public void testToString() {
        assertEquals(
                new MongoUUID("e98a7da1-fab2-4b29-9cc4-24a7b5b3d0d4").toString(),
                "e98a7da1-fab2-4b29-9cc4-24a7b5b3d0d4"
        );
    }

}
