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
import nl.xillio.xill.plugins.mongodb.data.MongoRegex;
import org.testng.annotations.Test;

import java.util.regex.PatternSyntaxException;

import static org.testng.Assert.assertEquals;

public class MongoRegexConstructTest extends TestUtils {
    @Test
    public void testConstruct() {
        MetaExpression result = process(new RegexConstruct(), fromValue(".*"));
        assertEquals(result.getStringValue(), ".*");
        assertEquals(result.getMeta(MongoRegex.class).getPattern().toString(), ".*");
    }

    @Test(expectedExceptions = PatternSyntaxException.class)
    public void testConstructWrongRegex() {
        process(new RegexConstruct(), fromValue("("));
    }
}
