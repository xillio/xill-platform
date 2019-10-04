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

import org.bson.types.Decimal128;
import org.testng.annotations.Test;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class Decimal128SerializerTest {
    private final Decimal128Serializer serializer = new Decimal128Serializer();

    @Test
    public void testParseObject() {
        assertEquals(
                serializer.parseObject(new Decimal128(5430462)),
                fromValue(5430462)
        );
        assertNull(serializer.parseObject("Test String"));
    }
}
