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
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.testng.annotations.Test;
import org.w3c.dom.Node;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

/**
 * Tests for the {@link GetTextConstructTest}
 *
 * @author Zbynek Hochmann
 */
public class GetTextConstructTest {

    /**
     * Test the process method under normal circumstances
     */
    @Test
    public void testProcess() {
        String text = "hello";

        // Mock
        Node node = mock(Node.class);
        when(node.getTextContent()).thenReturn(text);

        XmlNode xmlNode = mock(XmlNode.class);
        when(xmlNode.getText()).thenReturn(text);

        MetaExpression xmlNodeVar = mock(MetaExpression.class);
        when(xmlNodeVar.getMeta(XmlNode.class)).thenReturn(xmlNode);

        // Run
        MetaExpression result = GetTextConstruct.process(xmlNodeVar);

        // Verify
        verify(xmlNode).getText();

        // Assert
        assertSame(result.getStringValue(), text);
    }

    /**
     * Test the process when node input value is null
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Expected node to be a XML node")
    public void testProcessNull() {
        // Mock
        MetaExpression xmlNodeVar = mock(MetaExpression.class);
        when(xmlNodeVar.isNull()).thenReturn(true);

        // Run
        GetTextConstruct.process(xmlNodeVar);
    }
}