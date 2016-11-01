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
package nl.xillio.migrationtool.gui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import me.biesaart.utils.Log;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * Basic html viewport with core functions for highlighting nodes
 *
 * @author Xillio
 */
public class BrowserPane extends AnchorPane {
    private final Logger LOGGER = Log.get();

    public static enum ContentType {
        HTML, XML
    }

    ;

    public static final String CSS_PROPERTY_NAME = "xmt-property";


    protected WebView webView = new WebView();
    protected static XPath xpath = XPathFactory.newInstance().newXPath();
    private final MyNamespaceContext namespaceContext = new MyNamespaceContext();

    protected Node presentationNode;
    protected Node documentNode;
    protected ContentType contentType;

    private BrowserPane() {
        super();

        AnchorPane.setTopAnchor(webView, 0.0);
        AnchorPane.setBottomAnchor(webView, 0.0);
        AnchorPane.setLeftAnchor(webView, 0.0);
        AnchorPane.setRightAnchor(webView, 0.0);
        webView.getEngine().setUserStyleSheetLocation(Class.class.getResource("/preview-browser.css").toString());

        xpath.setNamespaceContext(namespaceContext);

        webView.getEngine().getLoadWorker().stateProperty().addListener((ChangeListener<State>) (ov, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                presentationNode = webView.getEngine().getDocument();
            }
        });

        getChildren().add(webView);
    }

    /**
     * Create a new BrowserPane in HTML mode, based on the provided url.
     *
     * @param uri
     * @param documentNode
     */
    public BrowserPane(final URL uri, final Node documentNode) {
        this();
        this.documentNode = documentNode;
        contentType = ContentType.HTML;

        webView.getEngine().load(uri.toExternalForm());
    }

    /**
     * Create a new BrowserPane in HTML mode, based on the provided url, and additional HTTP-Auth credentials
     *
     * @param uri
     * @param documentNode
     */
    public BrowserPane(final URL uri, final Node documentNode, final String user, final String pass) {
        this();

        Authenticator.setDefault(new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass.toCharArray());
            }
        });

        this.documentNode = documentNode;
        contentType = ContentType.HTML;

        webView.getEngine().load(uri.toExternalForm());
    }

    /**
     * Create a new BrowserPane in XML mode, based on the provided xml node.
     *
     * @param documentNode
     */
    public BrowserPane(final Node documentNode) {
        this();
        this.documentNode = documentNode;
        contentType = ContentType.XML;

        webView.getEngine().loadContent(xmlToHtml(documentNode));
    }

    public Node getDocumentNode() {
        return documentNode;
    }

    public Node getPresentationNode() {
        return presentationNode;
    }

    public String getUrl() {
        return webView.getEngine().getLocation();
    }

    public void highLight(final Node node) {
        if (node == null) {
            return;
        }

        // Skip if node is the document root.
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            return;
        }
        if (webView.getEngine().getLoadWorker().getState() == Worker.State.SUCCEEDED) {

            Node webNode = translateDocumentToPresentationNode(node);
            performHighLight(webNode);
        } else {
            webView.getEngine().getLoadWorker().stateProperty().addListener(
                    (ChangeListener<State>) (ov, oldState, newState) -> {
                        if (newState == Worker.State.SUCCEEDED) {
                            Platform.runLater(() -> {
                                Node webNode = translateDocumentToPresentationNode(node);
                                performHighLight(webNode);
                            });
                        }

                    });
        }

    }

    public void highLight(final List<Node> nodeList) {
        for (Node node : nodeList) {
            highLight(node);
        }
    }

    protected void performHighLight(final Node node) {
        if (node != null) {
            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    if ("br".equals(node.getLocalName())) {
                        encapsulate(node);
                    } else {
                        Element element = (Element) node;
                        element.setAttribute(CSS_PROPERTY_NAME, "selected");
                    }
                    break;
                case Node.TEXT_NODE:
                    encapsulate(node);
                    break;
                default:
                    break;
            }
        }
    }

    public void clearHighLights() {
        // 1. remove Capsulated nodes

        Object result = evaluateXPath(presentationNode, "//html:div[@" + CSS_PROPERTY_NAME + "='selection-capsule']", XPathConstants.NODESET);
        if (result != null) {
            NodeList nodeList = (NodeList) result;
            for (int i = 0; i < nodeList.getLength(); i++) {
                decapsulate(nodeList.item(i));
            }
        }

        // 2. remove class from other nodes
        result = evaluateXPath(presentationNode, "//*[@" + CSS_PROPERTY_NAME + "='selected']", XPathConstants.NODESET);
        if (result != null) {
            NodeList nodeList = (NodeList) result;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                element.removeAttribute(CSS_PROPERTY_NAME);
            }
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // Encapsulate functions

    /**
     * This puts an element in a div with an attribute allowing to highlight it in the WebView. It's useful for nodes that doesn't allow attributes (such
     * as text) or ones that are hard to highlight (such as br).
     *
     * @param node The node to encapsulate.
     */
    private void encapsulate(final Node node) {
        Element capsule = webView.getEngine().getDocument().createElement("div");
        capsule.setAttribute(CSS_PROPERTY_NAME, "selection-capsule");
        Element parent = (Element) node.getParentNode();
        parent.replaceChild(capsule, node);
        capsule.appendChild(node);
    }

    /**
     * Opposite method to encapsulate. Given a custom-made div, removes it from the parent, and connect its child instead.
     *
     * @param node The capsule to remove.
     * @throws IllegalArgumentException If the parent node hasn't the required xmt-property
     */
    private void decapsulate(final Node node) {
        Node child = node.getFirstChild();
        Node parent = node.getParentNode();
        if (!"DIV".equals(node.getNodeName()) || !"xmt-selection-capsule".equals(node.getAttributes().getNamedItem(CSS_PROPERTY_NAME).getNodeValue())) {
            // throw new IllegalArgumentException("Tried to decapsulate a non-encapsulated node.");
            return;
        }
        node.removeChild(child);
        parent.replaceChild(child, node);
    }

    // ////////////////////////////////////////////////////////////////////
    // XPath functions

    public Node translateDocumentToPresentationNode(final Node node) {
        if (node != null) {
            String presentationXpath = getPresentationXPath(node);
            return (Node) evaluateXPath(presentationNode, presentationXpath, XPathConstants.NODE);
        }
        return null;
    }

    public Object evaluateXPath(final Node source, final String query, final QName type) {
        try {
            Object result = xpath.evaluate(query, source, type);
            return result;
        } catch (XPathExpressionException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Gets the unmodified XPath for the provided node
     *
     * @param node
     * @return
     */
    public String getPlainXPath(final Node node) {
        String r = "";
        StringBuffer res = new StringBuffer();
        res.append("/" + node.getNodeName());

        int position = getPosition(node);
        if (position != 0) {
            res.append("[" + position + "]");
        }

        Node parent = node.getParentNode();
        while (parent != null && !"#document".equals(parent.getNodeName())) {

            position = getPosition(parent);
            res.insert(0, "/" + parent.getNodeName() + (position == 0 ? "" : "[" + position + "]"));

            parent = parent.getParentNode();
        }
        r = res.toString().toLowerCase().replaceAll("#([^/\\[]+)", "$1()");

        return r;
    }

    /**
     * Gets the document XPath for the provided presentation node
     *
     * @param node
     * @return
     */
    public String getDocumentXPath(final Node node) {
        String r = "";
        if (contentType == ContentType.HTML) {
            StringBuffer res = new StringBuffer();
            res.append("/" + node.getLocalName());

            int position = getPosition(node);
            if (position != 0) {
                res.append("[" + position + "]");
            }

            Node parent = node.getParentNode();
            while (parent != null && parent.getLocalName() != null && !"#document".equals(parent.getLocalName())) {

                position = getPosition(parent);
                res.insert(0, "/" + parent.getLocalName() + (position == 0 ? "" : "[" + position + "]"));

                parent = parent.getParentNode();
            }
            r = res.toString().replaceAll("#([^/\\[]+)", "$1()");
            r = r.replace("/tbody/", "/TBODY/"); // FIXME: Weird fix for HtmlUnit not registering tbody element properly
            r = r.replace("/html:", "/");

        } else if (contentType == ContentType.XML) {
            StringBuffer res = new StringBuffer();

            Node id = node.getAttributes().getNamedItem("id");
            String name = id != null ? id.getTextContent() : "";
            res.append("/" + name);

            int position = getPositionByID(node);
            if (position != 0) {
                res.append("[" + position + "]");
            }

            Node parent = node.getParentNode();
            while (parent != null && !"#document".equals(parent.getNodeName())) {

                id = parent.getAttributes().getNamedItem("id");
                name = id != null ? id.getTextContent() : null;

                if (name != null) {
                    position = getPositionByID(parent);
                    res.insert(0, "/" + name + (position == 0 ? "" : "[" + position + "]"));
                }
                parent = parent.getParentNode();
            }
            r = res.toString().toLowerCase().replaceAll("#([^/\\[]+)", "$1()");
        }

        r = r.replaceFirst("html\\[\\d+\\]", "html");

        if (documentNode.getNodeType() != Node.DOCUMENT_NODE) {
            r = "." + r;
        }

        if ("./".equals(r) || "".equals(r)) {
            r = ".";
        }

        return r;
    }

    /**
     * Gets the presentation XPath for the provided document node
     *
     * @param node
     * @return
     */
    public String getPresentationXPath(final Node node) {
        String r = "";
        if (contentType == ContentType.HTML) {
            // In html mode. Only need to prepend namespace prefixes, no xml->html translation required
            StringBuffer res = new StringBuffer();

            int position = 0;
            if (node.getNodeType() != Node.TEXT_NODE) {
                position = getPosition(node);
                res.append("/html:" + node.getNodeName() + (position != 0 ? "[" + position + "]" : ""));
            }

            Node parent = node.getParentNode();
            while (parent != null && !"#document".equals(parent.getNodeName())) {

                position = getPosition(parent);
                res.insert(0, "/html:" + parent.getNodeName() + (position == 0 ? "" : "[" + position + "]"));

                parent = parent.getParentNode();
            }
            r = res.toString();

        } else if (contentType == ContentType.XML) {
            // In xml mode. Need to convert from XML to html
            StringBuffer res = new StringBuffer();
            int position = 0;
            if (node.getNodeType() != Node.TEXT_NODE) {
                position = getPosition(node);
                res.append("/html:div[@id='" + node.getNodeName() + "'" + (position != 0 ? " and position()=" + position : "") + "]");
            }
            Node parent = node.getParentNode();
            while (parent != null && !"#document".equals(parent.getNodeName())) {

                position = getPosition(parent);
                res.insert(0, "/html:div[@id='" + parent.getNodeName() + "'" + (position != 0 ? " and position()=" + position : "") + "]");
                parent = parent.getParentNode();
            }
            r = res.toString();

        }

        r = r.toLowerCase().replaceAll("#([^/\\[]+)", "$1()");
        r = r.replace("/html:text()", "/text()");
        r = r.replaceFirst("html\\[\\d+\\]", "html");
        return "/" + r;

    }

    /**
     * Used by getXpath, to get the position of a node amongst it's siblings.
     *
     * @param node The node to get the position of.
     * @return 0 if the position is irrelevant, and the position of the node otherwise.
     */
    private static int getPosition(final Node node) {
        int position = 0;
        for (Node p = node.getPreviousSibling(); p != null; p = p.getPreviousSibling()) {
            if (p.getNodeName().equals(node.getNodeName())) {
                position++;
            }
        }
        if (position > 0) {
            position++;
        }
        for (Node p = node.getNextSibling(); position == 0 && p != null; p = p.getNextSibling()) {
            if (p.getNodeName().equals(node.getNodeName())) {
                position++;
            }
        }
        return position;

    }

    private static int getPositionByID(final Node node) {
        Node idattribute = node.getAttributes().getNamedItem("id");
        int index = 0, count = 0;
        if (idattribute != null) {
            String nodename = idattribute.getNodeValue();

            // Determine index

            for (int i = 0; i < node.getParentNode().getChildNodes().getLength(); i++) {
                Node c = node.getParentNode().getChildNodes().item(i);

                if (c == node) {
                    index = count;
                }

                if (c != null && c.getNodeType() == Node.ELEMENT_NODE) {
                    Node cidattribute = c.getAttributes().getNamedItem("id");
                    if (cidattribute != null) {
                        String cname = cidattribute.getNodeValue();
                        if (cname.equals(nodename)) {
                            count++;
                        }
                    }
                }
            }
        }
        return count > 1 ? index + 1 : 0;
    }

    /**
     * Convert XML to HTML that can be displayed
     *
     * @param xmlNode
     * @return
     */
    public static String xmlToHtml(final Node xmlNode) {
        String html = "<html>";
        html +=
                "<style>body{font-family:monospace;} span{display:inline;} pre{display:inline;} .indent{display:inline} .node{color:#666;} .nodename{color:#00d;} .attribute{color:#f00;} .attributevalue{color:#d0d;} .text{color:#000;} .comment{color:#0d0;} .cdata{color:#f80;}</style>";
        html += "<body>\n";

        html += nodeToHTML(xmlNode);
        html += "\n</body>\n</html>";
        return html;
    }

    private static int htmlindent = 0;

    private static String nodeToHTML(final Node node) {
        if (node == null) {
            return "";
        }

        String indent = StringUtils.repeat("<span class=\"indent\"><pre>  </pre></span>", htmlindent);

        StringBuilder text = new StringBuilder();

        switch (node.getNodeType()) {
            case Node.DOCUMENT_TYPE_NODE:
                text.append("<!doctype ").append(node.getNodeName()).append(">");
                break;
            case Node.DOCUMENT_NODE:
                if (node.hasChildNodes()) {
                    StringBuilder childhtml = new StringBuilder();
                    for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                        Node child = node.getChildNodes().item(i);
                        childhtml.append(nodeToHTML(child));
                    }
                    String chtml = childhtml.toString();
                    text.append(childhtml).append(chtml.contains("class=\"node\"") ? indent : "");
                }
                break;
            case Node.ELEMENT_NODE:
                String attributes = "";
                if (node.hasAttributes() && node.getAttributes() != null) {
                    StringBuilder attBuffer = new StringBuilder();
                    for (int j = 0, length = node.getAttributes().getLength(); j < length; j++) {
                        Node attr = node.getAttributes().item(j);
                        if (attr != null) {
                            attBuffer.append(" <span class=\"attribute\">").append(attr.getNodeName()).append("</span>=<span class=\"attributevalue\">\"").append(StringEscapeUtils.escapeXml(attr.getNodeValue()))
                                    .append("\"</span>");
                        }
                    }
                    attributes = attBuffer.toString();
                }

                text.append("\n<div class=\"node\" id=\"").append(node.getNodeName()).append("\">");
                text.append(indent).append("&lt;<span class=\"nodename\">").append(node.getNodeName()).append("</span>").append(attributes.length() > 0 ? " " + attributes : "").append("&gt;");
                if (node.hasChildNodes()) {
                    htmlindent++;
                    StringBuilder childhtml = new StringBuilder();
                    for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                        Node child = node.getChildNodes().item(i);
                        childhtml.append(nodeToHTML(child));
                    }
                    htmlindent--;
                    String chtml = childhtml.toString();
                    text.append(childhtml).append(chtml.contains("class=\"node\"") ? indent : "");
                }
                text.append("&lt;/<span class=\"nodename\">").append(node.getNodeName()).append("</span>&gt;</div>");
                break;
            case Node.TEXT_NODE:
                text.append("<span class=\"text\">").append(StringEscapeUtils.escapeXml(node.getNodeValue())).append("</span>");
                break;
            case Node.COMMENT_NODE:
                text.append("\n<div class=\"comment\">").append(indent).append("&lt;!--");
                text.append(node.getNodeValue());
                text.append("--&gt;</div>");
                break;
            case Node.CDATA_SECTION_NODE:
                text.append("\n<div class=\"cdata\">" + indent + "&lt;![CDATA[");
                text.append(StringEscapeUtils.escapeXml(node.getNodeValue()));
                text.append("]]&gt;</div>");
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                text.append("\n<div class=\"comment\">" + indent + "&lt;!-- Processing instruction --&gt;</div>");
                break;
            case Node.ATTRIBUTE_NODE:
                text.append("\n<div class=\"comment\">" + indent + "&lt;!-- Attribute node --&gt;</div>");
                break;
            case Node.ENTITY_REFERENCE_NODE:
                text.append("\n<div class=\"comment\">" + indent + "&lt;!-- Entity reference --&gt;</div>");
                break;
            case Node.DOCUMENT_FRAGMENT_NODE:
                text.append("\n<div class=\"comment\">" + indent + "&lt;!-- Document fragment --&gt;</div>");
                break;
            case Node.NOTATION_NODE:
                text.append("\n<div class=\"comment\">" + indent + "&lt;!-- Notation node --&gt;</div>");
                break;
            default:
                text.append("\n<div class=\"text\">" + node.getNodeValue() + "</div>"); // TODO: handle other node types: http://www.w3schools.com/jsref/dom_obj_node.asp
        }

        return text.toString().trim();
    }
    // ////////////////////////////////////////////////////////////////////
    // Innerclass for handling namespaces

    private class MyNamespaceContext implements NamespaceContext {

        /**
         * This bit is ugly. The javafx webview parses html documents with the "http://www.w3.org/1999/xhtml" namespace as default.
         * In combination with the Xerces XML parser, it will not properly handle omitted namespaces in XPaths. For this reason we add
         * a custom "html:" namespace prefix in our xpaths, which is handled here.
         * Note that it is not possible to hardcode blank namespaces to "http://www.w3.org/1999/xhtml" as omitted namespaces will not
         * trigger the use of the namespacecontext object. {@inheritDoc}
         */
        @Override
        public String getNamespaceURI(final String prefix) {
            if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                return webView.getEngine().getDocument().lookupNamespaceURI(null);

            } else if ("html".equals(prefix)) { // Ugly hardcoded namespace fix
                return "http://www.w3.org/1999/xhtml";
            } else {
                return webView.getEngine().getDocument().lookupNamespaceURI(prefix);
            }
        }

        @Override
        public String getPrefix(final String namespaceURI) {
            return webView.getEngine().getDocument().lookupPrefix(namespaceURI);
        }

        @Override
        public Iterator getPrefixes(final String arg0) {
            return null;
        }

    }

}
