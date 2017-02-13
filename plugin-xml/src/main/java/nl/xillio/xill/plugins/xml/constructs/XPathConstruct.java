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
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.data.XmlNode;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.xml.data.XmlNodeVar;
import nl.xillio.xill.plugins.xml.services.XpathService;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Returns selected XML node(s) from XML document using XPath locator.
 * Converts the output to facilitate interaction with Xill: single valued lists are converted to ATOMIC,
 * attributes are converted to OBJECT, text nodes of all kinds are converted to strings.
 *
 * @author Zbynek Hochmann
 * @author andrea.parrilli
 */
public class XPathConstruct extends Construct {
    @Inject
    private XpathService xpathService;

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (element, xPath, namespaces) -> process(element, xPath, namespaces, xpathService),
                new Argument("element", ATOMIC),
                new Argument("xPath", ATOMIC),
                new Argument("namespaces", NULL, OBJECT)
        );
    }

    @SuppressWarnings("unchecked")
    static MetaExpression process(MetaExpression elementVar, MetaExpression xpathVar, MetaExpression namespacesVar, XpathService service) {
        // Validate
        XmlNode node = assertMeta(elementVar, "node", XmlNode.class, "XML node");

        Map<String, String> namespaces = new LinkedHashMap<>();
        if (!namespacesVar.isNull()) {
            if (namespacesVar.getType() != ExpressionDataType.OBJECT) {
                throw new InvalidUserInputException("invalid namespace info: should be an OBJECT, got " + namespacesVar.getType().toString(),
                        namespacesVar.getStringValue(),
                        "an OBJECT containing the as keys the namespaces prefixes and as values the namespaces URIs");
            }
            for (Entry<String, MetaExpression> pair : ((Map<String, MetaExpression>) namespacesVar.getValue()).entrySet()) {
                namespaces.put(pair.getKey(), pair.getValue().getStringValue());
            }
        }

        String xpath = xpathVar.getStringValue();
        Object result = service.xpath(node, xpath, namespaces);
        return xpathResultToMetaExpression(result, service, xpath);
    }

    protected static MetaExpression xpathResultToMetaExpression(Object result, XpathService service, final String xpath) {
        if (result instanceof String) {
            return fromValue((String) result);
        } else if (result instanceof NodeList) {
            return xpathResultToMetaExpression((NodeList) result, service, xpath);
        } else if (result instanceof Node) {
            return xpathResultToMetaExpression((Node) result, xpath);
        } else {
            throw new OperationFailedException("extract information from XML node list", "unexpected result type: " + result.getClass().getSimpleName());
        }
    }

    protected static MetaExpression xpathResultToMetaExpression(final NodeList result, XpathService service, final String xpath) {
        int resultCardinality = result.getLength();

        if (resultCardinality == 0) {
            return NULL;
        } else if (resultCardinality == 1) {
            return xpathResultToMetaExpression(result.item(0), xpath);
        } else {
            return fromValue(service.asStream(result).map(n -> xpathResultToMetaExpression(n, xpath)).collect(Collectors.toList()));
        }
    }

    protected static MetaExpression xpathResultToMetaExpression(final Node node, final String xpath) {
        short nodeType = node.getNodeType();
        if (nodeType == Node.CDATA_SECTION_NODE ||
                nodeType == Node.ENTITY_NODE ||
                nodeType == Node.TEXT_NODE) {
            return fromValue(node.getTextContent());
        } else if (nodeType == Node.ATTRIBUTE_NODE) {
            return makeAttributeMetaExpression(node, xpath);
        } else {
            // XML nodes that cannot be converted to something xill: attach metadata expression
            return makeXmlNodeMetaExpression(node);
        }
    }

    protected static MetaExpression makeXmlNodeMetaExpression(final Node node) {
        MetaExpression output = fromValue(node.toString());
        output.storeMeta(new XmlNodeVar(node));
        return output;
    }

    protected static MetaExpression makeAttributeMetaExpression(final Node node, final String xpath) {
        // If the xpath ends with "@*" return an object with all the attributes, otherwise return the attribute value.
        if (xpath.endsWith("@*")) {
            LinkedHashMap<String, MetaExpression> attributeMap = new LinkedHashMap<>();
            attributeMap.put(node.getNodeName(), fromValue(node.getNodeValue()));
            return fromValue(attributeMap);
        } else {
            return fromValue(node.getNodeValue());
        }
    }
}
