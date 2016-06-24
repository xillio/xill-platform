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
package nl.xillio.xill.plugins.codec.decode.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.codec.decode.services.DecoderService;
import nl.xillio.xill.plugins.codec.decode.services.DecoderServiceImpl;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

/**
 * This is a test class for testing the FromPercentConstruct
 *
 * @author Pieter Dirk Soels
 */
public class FromPercentConstructTest extends TestUtils {

    @Test
    public void testDecodeFromPercent() throws IOException {
        // Initialize
        MetaExpression EXPECTED_TEXT = fromValue("Th!s is my e*pected string \\/\\/!th s0/\\/\\e weird ch@r@cters :D");
        MetaExpression DEPLOY_PERCENT = fromValue("Th%21s+is+my+e%2Apected+string+%5C%2F%5C%2F%21th+s0%2F%5C%2F%5Ce+weird+ch%40r%40cters+%3AD");

        DecoderService decoderService = new DecoderServiceImpl();
        FromPercentConstruct construct = new FromPercentConstruct(decoderService);
        ConstructContext context = mock(ConstructContext.class);

        ConstructProcessor processor = construct.prepareProcess(context);
        processor.setArgument(0, DEPLOY_PERCENT);

        // Run
        MetaExpression result = processor.process();

        // Assert
        assertEquals(result, EXPECTED_TEXT);
    }
}