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
package nl.xillio.xill.plugins.xml.constructs;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.data.XmlNode;
import nl.xillio.xill.plugins.xml.services.NodeService;
import nl.xillio.xill.plugins.xml.utils.MockUtils;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

/**
 * Tests for the {@link FromStringConstructTest}
 *
 * @author Zbynek Hochmann
 */
public class FromStringConstructTest {

    /**
     * Test the process method under normal circumstances
     */
    @Test
    public void testProcess() {
        // Mock
        String text = "xml test";
        XmlNode xmlNode = mock(XmlNode.class);
        when(xmlNode.toString()).thenReturn(text);

        NodeService nodeService = mock(NodeService.class);
        when(nodeService.fromString(anyString())).thenReturn(xmlNode);

        // Run
        MetaExpression result = FromStringConstruct.process(MockUtils.mockStringExpression(text), nodeService);

        // Verify
        verify(nodeService).fromString(any());

        // Assert
        assertSame(result.getMeta(XmlNode.class), xmlNode);
    }

}
