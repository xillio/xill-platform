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

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.data.XmlNode;
import nl.xillio.xill.plugins.xml.services.NodeService;

/**
 * Replaces existing node with new XML node
 *
 * @author Zbynek Hochmann
 */
public class ReplaceNodeConstruct extends Construct {
    @Inject
    private NodeService nodeService;

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (node, xml) -> process(node, xml, nodeService),
                new Argument("node", ATOMIC),
                new Argument("xml", ATOMIC)
        );
    }

    static MetaExpression process(MetaExpression orgNodeVar, MetaExpression replXmlStrVar, NodeService service) {
        XmlNode orgXmlNode = assertMeta(orgNodeVar, "node", XmlNode.class, "XML node");
        String replXmlStr = replXmlStrVar.getStringValue();

        XmlNode newNode = service.replaceNode(orgXmlNode, replXmlStr);

        MetaExpression result = fromValue(newNode.toString());
        result.storeMeta(newNode);
        return result;
    }

}
