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
package nl.xillio.xill.plugins.codec.encode.constructs;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.codec.decode.constructs.UnescapeXMLConstruct;
import nl.xillio.xill.plugins.codec.encode.services.EncoderService;
import nl.xillio.xill.plugins.codec.encode.services.EncoderServiceImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Test the {@link UnescapeXMLConstruct}.
 */
public class EscapeXMLConstructTest {

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processNormalUsage() {
        // Mock
        String stringValue = "<p><a href=\"default.asp\">HTML Tutorial</a> This is a link to a page on this website.</p>";
        MetaExpression string = mock(MetaExpression.class);
        when(string.getStringValue()).thenReturn(stringValue);

        String returnValue = "&lt;p&gt;&lt;a href=&quot;default.asp&quot;&gt;HTML Tutorial&lt;/a&gt; This is a link to a page on this website.&lt;/p&gt";
        EncoderService encoderService = mock(EncoderServiceImpl.class);
        EscapeXMLConstruct construct = new EscapeXMLConstruct(encoderService);
        when(encoderService.escapeXML(stringValue)).thenReturn(returnValue);
        // Run
        MetaExpression result = construct.process(string);

        // Verify
        verify(encoderService, times(1)).escapeXML(stringValue);

        // Assert
        Assert.assertEquals(result.getStringValue(), returnValue);
    }
}
