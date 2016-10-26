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
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.string.services.string.StringUtilityService;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Test the {@link ByteLengthConstruct}.
 */
public class ByteLengthConstructTest extends TestUtils {

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processNormalUsage() {

        int result = process(new ByteLengthConstruct(), fromValue("foo"), NULL).getNumberValue().intValue();
        Assert.assertEquals(result, 3);

        result = process(new ByteLengthConstruct(), fromValue("foo"), fromValue("ASCII")).getNumberValue().intValue();
        Assert.assertEquals(result, 3);

        result = process(new ByteLengthConstruct(), fromValue("foo"), fromValue("UTF-8")).getNumberValue().intValue();
        Assert.assertEquals(result, 3);

        result = process(new ByteLengthConstruct(), fromValue("foo"), fromValue("UTF-16")).getNumberValue().intValue();
        Assert.assertEquals(result, 8);

        result = process(new ByteLengthConstruct(), fromValue("foo"), fromValue("UTF-32")).getNumberValue().intValue();
        Assert.assertEquals(result, 12);


    }

    /**
     * Test the process when the given string is null.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void processNullValue() {
        ByteLengthConstruct construct = new ByteLengthConstruct();
        process(construct, NULL, NULL);
    }

    /**
     * Test the process when the given encoding is invalid.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void processInvalidEncoding() {
        ByteLengthConstruct construct = new ByteLengthConstruct();
        process(construct, fromValue("foo"), fromValue("invalid encoding"));
    }
}
