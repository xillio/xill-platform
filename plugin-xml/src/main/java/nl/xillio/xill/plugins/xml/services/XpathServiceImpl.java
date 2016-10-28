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
package nl.xillio.xill.plugins.xml.services;

import com.google.inject.Singleton;
import me.biesaart.utils.Log;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.xpath.XPathExpressionImpl;
import net.sf.saxon.xpath.XPathFactoryImpl;
import nl.xillio.xill.api.data.XmlNode;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.xml.data.XmlNodeVar;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is the main implementation of the {@link XpathService}
 *
 * @author Zbynek Hochmann
 */

@Singleton
public class XpathServiceImpl implements XpathService {

    private static final XPathFactory xpf = new XPathFactoryImpl();

    private static final Logger LOGGER = Log.get();

    @Override
    public List<Object> xpath(final XmlNode node, final String xpathQuery, final Map<String, String> namespaces) {
        HTMLNamespaceContext namespaceContext = new HTMLNamespaceContext(namespaces);

        XPath xpath = xpf.newXPath();
        xpath.setNamespaceContext(namespaceContext);
        ArrayList<Object> output = new ArrayList<>();

        boolean fetchText = xpathQuery.endsWith("/text()");

        // More hacking... the java implementation bugs out on selecting a CDATA textnode.
        // We will need to first query the node, then do another query to fetch the textual content.
        String query = xpathQuery;
        if (fetchText) {
            query = xpathQuery.substring(0, xpathQuery.length() - "/text()".length());
        }

        try {
            Document document = node.getDocument();
            namespaceContext.setDocument(document);

            Object result = this.evaluateExpression(this.compileXpath(xpath,query),node.getNode());
            if (result instanceof NodeList) {
                NodeList results = (NodeList) result;

                for (int i = 0; i < results.getLength(); i++) {
                    Node n = results.item(i);
                    output.add(fetchText ? xPathText(xpath, n, "./text()") : parseVariable(n));
                }
            } else {
                output.add(result.toString());
            }
        } catch (XPathExpressionException e) {
            throw new RobotRuntimeException("Invalid XPath", e);
        }

        return output;
    }

    private XPathExpression compileXpath(final XPath xpath, final String expression) throws XPathExpressionException {
        try {
            return xpath.compile(expression);
        } catch (Exception e) { // Sometimes, an unexpected net.sf.saxon.trans.XPathException can be thrown...
            LOGGER.error("Failed to run xpath expression", e);
            throw new XPathExpressionException(e.getMessage());
        }
    }

    private Object evaluateExpression(XPathExpression expr, final Object node) throws XPathExpressionException {
        try {
            return expr.evaluate(node, computeExpressionResultType(expr));
        } catch (Exception e) {
            LOGGER.warn("Exception while evaluating xpath expression", e);
        }

        return expr.evaluate(node, XPathConstants.STRING);
    }

    private QName computeExpressionResultType(XPathExpression expr) {
        Expression innerExpr = ((XPathExpressionImpl) expr).getInternalExpression();
        ItemType resultType = innerExpr.getItemType();

        if(resultType.isAtomicType()) {
            return XPathConstants.STRING;
        }
        else {
            return XPathConstants.NODESET;
        }
    }

    private String xPathText(final XPath xpath, final Object node, final String expression) throws XPathExpressionException {
        return xpath.compile(expression).evaluate(node).trim();
    }

    private static Object parseVariable(final Node node) {
        switch (node.getNodeType()) {
            case Node.COMMENT_NODE:
            case Node.ATTRIBUTE_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                return node.getNodeValue();
            default:
                return new XmlNodeVar(node);
        }
    }

    /**
     * Innerclass for handling XML namespaces
     */
    private class HTMLNamespaceContext implements NamespaceContext {
        private Document document;
        private static final String URI = "http://www.w3.org/1999/xhtml";
        private final Map<String, String> namespaces;

        public HTMLNamespaceContext(final Map<String, String> namespaces) {
            this.namespaces = namespaces;
        }

        @Override
        public String getNamespaceURI(final String prefix) {
            if (document == null) {
                return URI;
            } else if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                return document.lookupNamespaceURI(null);
            } else if (namespaces.containsKey(prefix)) {
                return namespaces.get(prefix);
            } else {
                return document.lookupNamespaceURI(prefix);
            }
        }

        @Override
        public String getPrefix(final String namespaceURI) {
            return document == null ? URI : document.lookupPrefix(namespaceURI);
        }

        @Override
        public Iterator<?> getPrefixes(final String arg0) {
            return null;
        }

        public void setDocument(final Document document) {
            this.document = document;
        }
    }

}