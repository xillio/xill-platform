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
package nl.xillio.xill.plugins.xml.data;

import me.biesaart.utils.Log;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.preview.TextPreview;
import nl.xillio.xill.plugins.xml.exceptions.XmlParseException;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class represents a XML node MetadataExpression
 *
 * @author Zbynek Hochmann
 */
public class XmlNodeVar implements nl.xillio.xill.api.data.XmlNode, TextPreview {

    private static final Logger LOGGER = Log.get();
    private Node node = null;
    private boolean treatAsDocument = false;

    TransformerFactory tf = TransformerFactory.newInstance();

    /**
     * Creates XmlNode from XML string
     *
     * @param xmlString       XML document
     * @param treatAsDocument true if parsing XML document, false if this meant to be just XML node
     * @throws XmlParseException when XML format is invalid
     */
    public XmlNodeVar(final String xmlString, final boolean treatAsDocument) throws XmlParseException {
        this.treatAsDocument = treatAsDocument;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new InputSource(new StringReader(xmlString)));

            // Normalize whitespace nodes
            removeEmptyTextNodes(document);
            document.normalize();
            this.node = document.getFirstChild();
        } catch (SAXParseException e) {
            throw new XmlParseException(e.getMessage(), e);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Creates XmlNode from org.w3c.dom.Node
     *
     * @param node input node
     */
    public XmlNodeVar(Node node) {
        this.node = node;
    }

    /**
     * Returns XML document of this node
     *
     * @return org.w3c.dom.Document of this node
     */
    @Override
    public Document getDocument() {
        return this.node.getOwnerDocument();
    }

    /**
     * @return org.w3c.dom.Node data specifying this node
     */
    @Override
    public Node getNode() {
        return this.node;
    }

    @Override
    public String toString() {
        if (this.node == null) {
            return "XML Node[null]";
        }
        if (this.treatAsDocument) {
            return String.format("XML Document[first node = %1$s]", this.node.getNodeName());
        } else {
            return String.format("XML Node[%1$s]", this.node.getNodeName());
        }
    }

    /**
     * @return a string containing all text extracted from XML node or XML document
     */
    @Override
    public String getText() {
        NodeList list = this.getNode().getChildNodes();

        return IntStream.range(0, list.getLength())
                .mapToObj(list::item)
                .map(Node::getTextContent)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Returns XML content of this node in string format
     *
     * @return XML content in string format
     */
    @Override
    public String getXmlContent() {
        if (this.node == null) {
            return "null";
        }

        try {
            DOMSource domSource = new DOMSource(this.treatAsDocument ? this.getDocument() : this.node);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (Exception e) {
            LOGGER.error("Error while formatting XML", e);
        }
        return null;
    }

    /**
     * Returns XML content of this node in string format
     *
     * @param maxSize The maximum text size of returned content
     * @return XML content in string format
     */
    public String getXmlContent(int maxSize) {
        if (this.node == null) {
            return "null";
        }

        try {
            DOMSource domSource = new DOMSource(this.treatAsDocument ? this.getDocument() : this.node);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, result);

            if (writer.getBuffer().length() > maxSize) {
                return writer.getBuffer().substring(0, maxSize - NOT_COMPLETE_MARK.length()) + NOT_COMPLETE_MARK;
            } else {
                return writer.toString();
            }
        } catch (Exception e) {
            LOGGER.error("Error while formatting XML", e);
        }
        return null;
    }

    private void removeEmptyTextNodes(final Node parent) {
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            Node child = parent.getChildNodes().item(i);
            if (child.hasChildNodes()) {
                removeEmptyTextNodes(child);
            } else if (child.getNodeType() == Node.TEXT_NODE && child.getNodeValue().trim().isEmpty()) {
                parent.removeChild(child);
                i--;
            }
        }
    }

    @Override
    public String getTextPreview() {
        return getXmlContent(MAX_TEXT_PREVIEW_SIZE);
    }

}
