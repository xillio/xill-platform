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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.data.XmlNode;
import nl.xillio.xill.plugins.xml.services.NodeService;
import nl.xillio.xill.services.files.TextFileReader;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;

/**
 * Tests for the {@link FromFileConstructTest}
 *
 * @author Zbynek Hochmann
 */
public class FromFileConstructTest extends TestUtils {
    private NodeService nodeService = mock(NodeService.class);
    private TextFileReader textFileReader = mock(TextFileReader.class);
    private FromFileConstruct construct = new FromFileConstruct(nodeService, textFileReader);

    /**
     * Test the construct under normal circumstances.
     */
    @Test
    public void testProcess() throws IOException {
        String source = "<parent><child>inner</child></parent>";

        // Mock.
        when(textFileReader.getText(any(), any())).thenReturn(source);
        when(nodeService.fromString(anyString())).thenReturn(mock(XmlNode.class));

        // Run.
        MetaExpression result = this.process(construct, fromValue(""));

        // Verify.
        verify(nodeService).fromString(source);

        // Assert.
        assertTrue(result.hasMeta(XmlNode.class));
    }
}