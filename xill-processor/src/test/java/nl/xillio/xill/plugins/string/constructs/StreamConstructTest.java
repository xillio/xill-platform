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
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

public class StreamConstructTest extends TestUtils {
    StreamConstruct construct = new StreamConstruct();

    @Test
    public void testNormal() throws IOException {
        String value = "Stream me!";

        MetaExpression result = process(construct, fromValue(value));
        String resultString = IOUtils.toString(result.getBinaryValue().getInputStream());

        assertEquals(resultString, value);
    }

    @Test
    public void testNull() throws IOException {
        MetaExpression result = process(construct, NULL);
        String resultString = IOUtils.toString(result.getBinaryValue().getInputStream());

        assertEquals(resultString, "");
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testIllegalCharsetName() throws IOException {
        process(construct, fromValue("Hello World"), fromValue("no exist"));
    }


    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testNonExistingCharset() throws IOException {
        process(construct, fromValue("Hello World"), fromValue("UTF-9"));
    }

    @Test
    public void testSpecialCharacters() throws IOException {
        String value = "διακριτικός";

        MetaExpression result = process(construct, fromValue(value), fromValue("UTF-8"));
        String resultString = IOUtils.toString(result.getBinaryValue().getInputStream());

        assertEquals(resultString, value);
    }
}
