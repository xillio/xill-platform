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
import static org.testng.Assert.assertTrue;

/**
 * Tests for the {@link MoveNodeConstructTest}
 *
 * @author Zbynek Hochmann
 */
public class MoveNodeConstructTest {

    /**
     * Test the process method under normal circumstances
     */
    @Test
    public void testProcess() {
        // Mock
        NodeService nodeService = mock(NodeService.class);

        XmlNode parentXmlNode = mock(XmlNode.class);
        MetaExpression parentXmlNodeVar = mock(MetaExpression.class);
        when(parentXmlNodeVar.getMeta(XmlNode.class)).thenReturn(parentXmlNode);

        XmlNode subXmlNode = mock(XmlNode.class);
        MetaExpression subXmlNodeVar = mock(MetaExpression.class);
        when(subXmlNodeVar.getMeta(XmlNode.class)).thenReturn(subXmlNode);

        // Run
        MetaExpression result = MoveNodeConstruct.process(parentXmlNodeVar, subXmlNodeVar, MockUtils.mockNullExpression(), nodeService);

        // Verify
        verify(nodeService).moveNode(any(), any(), any());

        // Assert
        assertTrue(result.isNull());
    }

    /**
     * Test the process when the first input value is null
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Expected node to be a XML node")
    public void testProcessNullParent() {
        // Mock
        NodeService nodeService = mock(NodeService.class);

        XmlNode subXmlNode = mock(XmlNode.class);
        MetaExpression subXmlNodeVar = mock(MetaExpression.class);
        when(subXmlNodeVar.getMeta(XmlNode.class)).thenReturn(subXmlNode);

        // Run
        MoveNodeConstruct.process(MockUtils.mockNullExpression(), subXmlNodeVar, MockUtils.mockNullExpression(), nodeService);
    }

    /**
     * Test the process when the second input value is null
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Expected node to be a XML node")
    public void testProcessNullSub() {
        // Mock
        NodeService nodeService = mock(NodeService.class);

        XmlNode parentXmlNode = mock(XmlNode.class);
        MetaExpression parentXmlNodeVar = mock(MetaExpression.class);
        when(parentXmlNodeVar.getMeta(XmlNode.class)).thenReturn(parentXmlNode);

        // Run
        MoveNodeConstruct.process(parentXmlNodeVar, MockUtils.mockNullExpression(), MockUtils.mockNullExpression(), nodeService);
    }
}