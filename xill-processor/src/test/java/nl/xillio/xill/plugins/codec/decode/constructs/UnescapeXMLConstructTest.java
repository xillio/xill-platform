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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.codec.decode.services.DecoderService;
import nl.xillio.xill.plugins.codec.decode.services.DecoderServiceImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Test the {@link UnescapeXMLConstruct}.
 */
public class UnescapeXMLConstructTest {

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processNormalUsage() {
        // Mock
        String stringValue = "Money &lt;&amp;gt; Health";
        MetaExpression string = mock(MetaExpression.class);
        when(string.getStringValue()).thenReturn(stringValue);

        int passesValue = 2;
        MetaExpression passes = mock(MetaExpression.class);
        when(passes.getNumberValue()).thenReturn(passesValue);

        String returnValue = "Money <> Health";
        DecoderService decoderService = mock(DecoderServiceImpl.class);
        UnescapeXMLConstruct construct = new UnescapeXMLConstruct(decoderService);
        when(decoderService.unescapeXML(stringValue, passesValue)).thenReturn(returnValue);
        // Run
        MetaExpression result = construct.process(string, passes);

        // Verify
        verify(decoderService, times(1)).unescapeXML(stringValue, passesValue);

        // Assert
        Assert.assertEquals(result.getStringValue(), returnValue);
    }
}
