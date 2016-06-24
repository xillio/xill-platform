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
package nl.xillio.xill.plugins.codec.decode.services;

import nl.xillio.xill.TestUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * This is the test class for the
 *
 * @author Pieter Dirk Soels
 */
public class DecoderServiceImplTest extends TestUtils {

    private final DecoderService decoderService = new DecoderServiceImpl();

    @Test
    public void testUnescapeXML() {
        String text = "&lt;test&gt;&quot;&amp;";
        int passes = 5;

        String result = decoderService.unescapeXML(text, passes);

        String expectedOutput = "<test>\"&";
        assertEquals(expectedOutput, result);
    }
}