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
import nl.xillio.xill.plugins.mongodb.data.MongoUUID;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.assertEquals;

public class UuidConstructTest extends TestUtils {
    @Test
    public void testConstruct() {
        MetaExpression result = process(new UuidConstruct(), fromValue("7e713129-d14c-4a1c-aca1-0b3d11d9c9c7"));
        assertEquals(
                result.getStringValue(),
                "7e713129-d14c-4a1c-aca1-0b3d11d9c9c7"
        );
        assertEquals(
                result.getMeta(MongoUUID.class).getUuid(),
                UUID.fromString("7e713129-d14c-4a1c-aca1-0b3d11d9c9c7")
        );
    }

    @Test
    public void testGenerateUuid() {
        MetaExpression result = process(new UuidConstruct(), NULL);
        UUID uuid = result.getMeta(MongoUUID.class).getUuid();

        assertEquals(
                result.getStringValue(),
                uuid.toString()
        );
    }
}
