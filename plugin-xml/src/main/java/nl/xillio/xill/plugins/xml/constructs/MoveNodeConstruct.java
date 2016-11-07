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
 * Moves existing node into different position in existing XML document.
 *
 * @author Zbynek Hochmann
 */
public class MoveNodeConstruct extends Construct {
    public static final String XML_NODE = "XML node";
    @Inject
    private NodeService nodeService;

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (parentNode, subnode, beforeNode) -> process(parentNode, subnode, beforeNode, nodeService),
                new Argument("parentNode", ATOMIC),
                new Argument("subnode", ATOMIC),
                new Argument("beforeNode", NULL, ATOMIC)
        );
    }

    static MetaExpression process(MetaExpression parentNodeVar, MetaExpression subnodeVar, MetaExpression beforeNodeVar, NodeService service) {
        XmlNode parentXmlNode = assertMeta(parentNodeVar, "node", XmlNode.class, XML_NODE);
        XmlNode subXmlNode = assertMeta(subnodeVar, "node", XmlNode.class, XML_NODE);
        XmlNode beforeXmlNode = beforeNodeVar.isNull() ? null : assertMeta(beforeNodeVar, "node", XmlNode.class, XML_NODE);

        service.moveNode(parentXmlNode, subXmlNode, beforeXmlNode);
        return NULL;
    }

}
