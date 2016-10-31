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
import nl.xillio.xill.plugins.xml.services.XpathService;
import nl.xillio.xill.plugins.xml.utils.MockUtils;
import org.testng.annotations.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

/**
 * Tests for the {@link XpathConstructTest}
 *
 * @author Zbynek Hochmann
 */
public class XpathConstructTest {

    /**
     * Test the process method under normal circumstances
     */
    @Test
    public void testProcess() {
        // Mock
        NodeList returnXmlNodeList = mock(NodeList.class);
        Node resultNode = mock(Node.class);
        when(returnXmlNodeList.getLength()).thenReturn(1);
        when(returnXmlNodeList.item(0)).thenReturn(resultNode);

        XpathService xpathService = mock(XpathService.class);
        when(xpathService.xpath(any(), anyString(), any())).thenReturn(returnXmlNodeList);

        XmlNode xmlNode = mock(XmlNode.class);
        MetaExpression xmlNodeVar = mock(MetaExpression.class);
        when(xmlNodeVar.getMeta(XmlNode.class)).thenReturn(xmlNode);

        String text = "xpath selector";
        MetaExpression textVar = MockUtils.mockStringExpression(text);

        // Run
        MetaExpression result = XPathConstruct.process(xmlNodeVar, textVar, MockUtils.mockNullExpression(), xpathService);

        // Verify
        verify(xpathService).xpath(any(), anyString(), any());

        // Assert
        assertSame(result.getMeta(XmlNode.class).getNode(), resultNode);
    }

    /**
     * Test the process when node input value is null
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Expected node to be a XML node")
    public void testProcessNodeNull() {

        // Mock
        XpathService xpathService = mock(XpathService.class);

        String text = "xpath selector";
        MetaExpression textVar = MockUtils.mockStringExpression(text);

        MetaExpression xmlNodeVar = mock(MetaExpression.class);
        when(xmlNodeVar.isNull()).thenReturn(true);

        // Run
        XPathConstruct.process(xmlNodeVar, textVar, MockUtils.mockNullExpression(), xpathService);
    }
}