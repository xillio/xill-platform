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
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;
import org.slf4j.Logger;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.*;
import java.util.Iterator;
import java.util.Map;

/**
 * This class is the main implementation of the {@link XpathService}
 *
 * @author Zbynek Hochmann
 * @author andrea.parrilli
 */

@Singleton
public class XpathServiceImpl implements XpathService {
    private static final XPathFactory xpf = new XPathFactoryImpl();
    private static final Logger LOGGER = Log.get();


    @Override
    public Object xpath(final XmlNode node, final String xpathQuery, final Map<String, String> namespaces) {
        XPath xpath = makeXpathWithNamespaces(node, namespaces);
        Object result;
        XPathExpression compiledExpression;

        // Compile
        try {
            compiledExpression = compileXpath(xpath, xpathQuery);
        } catch (XPathExpressionException e) {
            throw new InvalidUserInputException("xpath is not valid", xpathQuery, "a valid xpath", e);
        }

        // Execute
        try {
            result = compiledExpression.evaluate(node.getNode(), computeExpressionResultType(compiledExpression));
        } catch(XPathExpressionException e) {
            throw new OperationFailedException("processing xpath " + xpathQuery, "processing terminated prematurely", e);
        }

        return result;
    }


    // sets the namespaces for this xpath compilation
    private XPath makeXpathWithNamespaces(final XmlNode node, final Map<String, String> namespaces) {
        HTMLNamespaceContext namespaceContext = new HTMLNamespaceContext(namespaces);
        XPath xpath = xpf.newXPath();
        xpath.setNamespaceContext(namespaceContext);
        Document document = node.getDocument();
        namespaceContext.setDocument(document);

        return xpath;
    }


    private XPathExpression compileXpath(final XPath xpath, final String expression) throws XPathExpressionException {
        try {
            return xpath.compile(expression);
        } catch (Exception e) { // Sometimes, an unexpected net.sf.saxon.trans.XPathException can be thrown...
            LOGGER.error("Failed to run xpath expression", e);
            throw new XPathExpressionException(e.getMessage());
        }
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


    /**
     * Inner class for handling XML namespaces.
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