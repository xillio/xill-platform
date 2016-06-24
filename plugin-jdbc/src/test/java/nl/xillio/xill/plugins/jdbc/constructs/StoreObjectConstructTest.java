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
package nl.xillio.xill.plugins.jdbc.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.jdbc.services.ExpressionConverter;
import org.testng.annotations.Test;

/**
 * For tests for the successful path, check out {@link nl.xillio.xill.plugins.jdbc.H2IntegrationTest}.
 */
public class StoreObjectConstructTest extends TestUtils {

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*fields.*")
    public void testExceptionOnNoFieldInput() {
        StoreObjectConstruct construct = new StoreObjectConstruct(null, new ExpressionConverter(), null, null, null);

        process(construct, fromValue("table"), emptyObject());
    }
}