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
package nl.xillio.xill.plugins.string.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class IsEmptyConstructTest extends TestUtils {
    private IsEmptyConstruct construct = new IsEmptyConstruct();

    @Test
    public void testProcess() {
        // Tests.
        Map<MetaExpression, Boolean> tests = new HashMap<>();
        tests.put(fromValue("foo"), false);
        tests.put(NULL, true);
        tests.put(fromValue(0), false);
        tests.put(fromValue(""), true);

        // Process, assert.
        tests.forEach((m, b) -> Assert.assertEquals(this.process(construct, m).getBooleanValue(), b.booleanValue()));
    }
}
