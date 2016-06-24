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
import nl.xillio.xill.plugins.xml.services.NodeService;
import nl.xillio.xill.plugins.xml.utils.MockUtils;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

/**
 * Tests for the {@link InsertNodeConstructTest}
 *
 * @author Zbynek Hochmann
 */
public class InsertNodeConstructTest {

    /**
     * Test the process method under normal circumstances
     */
    @Test
    public void testProcess() {
        // Mock
        XmlNode returnXmlNode = mock(XmlNode.class);

        NodeService nodeService = mock(NodeService.class);
        when(nodeService.insertNode(any(), anyString(), any())).thenReturn(returnXmlNode);

        XmlNode xmlNode = mock(XmlNode.class);
        MetaExpression xmlNodeVar = mock(MetaExpression.class);
        when(xmlNodeVar.getMeta(XmlNode.class)).thenReturn(xmlNode);

        String text = "test";
        MetaExpression textVar = MockUtils.mockStringExpression(text);

        // Run
        MetaExpression result = InsertNodeConstruct.process(xmlNodeVar, textVar, MockUtils.mockNullExpression(), nodeService);

        // Verify
        verify(nodeService).insertNode(any(), anyString(), any());

        // Assert
        assertSame(result.getMeta(XmlNode.class), returnXmlNode);
    }

    /**
     * Test the process when the input is invalid XML
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Function insertNode parse error.*")
    public void testProcessInvalidXml() {
        // Mock
        String text = "<a>B<c>";
        MetaExpression textVar = MockUtils.mockStringExpression(text);

        NodeService nodeService = mock(NodeService.class);
        when(nodeService.insertNode(any(), anyString(), any())).thenThrow(new RobotRuntimeException("Function insertNode parse error!\n This mocking is really odd.."));

        XmlNode xmlNode = mock(XmlNode.class);
        MetaExpression xmlNodeVar = mock(MetaExpression.class);
        when(xmlNodeVar.getMeta(XmlNode.class)).thenReturn(xmlNode);

        // Run
        InsertNodeConstruct.process(xmlNodeVar, textVar, MockUtils.mockNullExpression(), nodeService);
    }

    /**
     * Test the process when node input value is null
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Expected node to be a XML node")
    public void testProcessNodeNull() {

        // Mock
        NodeService nodeService = mock(NodeService.class);

        String text = "test";
        MetaExpression textVar = MockUtils.mockStringExpression(text);

        MetaExpression xmlNodeVar = mock(MetaExpression.class);
        when(xmlNodeVar.isNull()).thenReturn(true);

        // Run
        InsertNodeConstruct.process(xmlNodeVar, textVar, MockUtils.mockNullExpression(), nodeService);
    }
}