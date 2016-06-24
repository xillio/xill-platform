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
import nl.xillio.xill.api.data.XmlNode;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.xml.data.XmlNodeVar;
import nl.xillio.xill.plugins.xml.exceptions.XmlParseException;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

/**
 * This class is the main implementation of the {@link NodeService}
 *
 * @author Zbynek Hochmann
 */

@Singleton
public class NodeServiceImpl implements NodeService {
    private static final Pattern LEADING_BOM_PATTERN = Pattern.compile("^\uFEFF+"); // Duplicated in: File::GetTextConstruct.

    @Override
    public XmlNode insertNode(final XmlNode parentXmlNode, final String newChildNodeStr, final XmlNode beforeChildXmlNode) {
        XmlNodeVar newXmlChildNode = null;
        try {
            newXmlChildNode = new XmlNodeVar(newChildNodeStr, false);
        } catch (XmlParseException e) {
            throw new RobotRuntimeException("Function insertNode parse error!\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new RobotRuntimeException("Function insertNode error!\n" + e.getMessage(), e);
        }

        Node parentNode = parentXmlNode.getNode();
        Node beforeChildNode = beforeChildXmlNode == null ? null : beforeChildXmlNode.getNode();
        Node newNode = parentXmlNode.getDocument().importNode(newXmlChildNode.getNode(), true);

        if (beforeChildNode == null || !beforeChildNode.getParentNode().equals(parentNode)) {
            parentNode.appendChild(newNode);
        } else {
            parentNode.insertBefore(newNode, beforeChildNode);
        }

        return new XmlNodeVar(newNode);
    }

    @Override
    public void moveNode(final XmlNode parentXmlNode, final XmlNode subXmlNode, final XmlNode beforeXmlNode) {
        Node parentNode = parentXmlNode.getNode();
        Node subNode = subXmlNode.getNode();
        Node beforeNode = beforeXmlNode == null ? null : beforeXmlNode.getNode();

        // If by accident we ended up with a document, get the root child node
        if (subNode instanceof Document) {
            subNode = subNode.getFirstChild();
        }

        // If we are moving/copying the node from one document to another, register the node first.
        if (!parentNode.getOwnerDocument().equals(subNode.getOwnerDocument())) {
            subNode = parentNode.getOwnerDocument().importNode(subNode, true);
        }

        // Register the node at the new position
        if (beforeNode == null || !beforeNode.getParentNode().equals(parentNode)) {
            parentNode.appendChild(subNode);
        } else {
            parentNode.insertBefore(subNode, beforeNode);
        }
    }

    @Override
    public XmlNode replaceNode(final XmlNode orgXmlNode, final String replXmlStr) {
        XmlNodeVar replXmlNode = null;
        try {
            replXmlNode = new XmlNodeVar(replXmlStr, false);
        } catch (XmlParseException e) {
            throw new RobotRuntimeException("Function replaceNode parse error!\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new RobotRuntimeException("Function replaceNode error!\n" + e.getMessage(), e);
        }
        Node orgNode = orgXmlNode.getNode();
        Node newReplNode = orgXmlNode.getDocument().importNode(replXmlNode.getNode(), true);
        orgNode.getParentNode().replaceChild(newReplNode, orgNode);
        return replXmlNode;
    }

    @Override
    public void removeNode(final XmlNode xmlNode) {
        Node node = xmlNode.getNode();
        node.getParentNode().removeChild(node);
    }

    @Override
    public void setAttribute(final XmlNode xmlNode, final String attrName, final String attrValue) {
        NamedNodeMap attributes = xmlNode.getNode().getAttributes();
        Node attNode = xmlNode.getDocument().createAttribute(attrName);
        attNode.setNodeValue(attrValue);
        attributes.setNamedItem(attNode);
    }

    @Override
    public boolean removeAttribute(final XmlNode xmlNode, final String attrName) {
        NamedNodeMap attributes = xmlNode.getNode().getAttributes();
        if (attributes.getNamedItem(attrName) == null) {
            return false;
        } else {
            attributes.removeNamedItem(attrName);
            return true;
        }
    }

    @Override
    public XmlNode fromFilePath(final Path xmlSource) {
        String content;

        try (InputStream stream = Files.newInputStream(xmlSource, StandardOpenOption.READ);) {
            content = IOUtils.toString(stream);
        } catch (IOException e) {
            throw new RobotRuntimeException("Read file error.", e);
        }

        // Remove leading BOM characters.
        content = LEADING_BOM_PATTERN.matcher(content).replaceFirst("");

        if (content.isEmpty()) {
            throw new RobotRuntimeException("The file is empty.");
        }

        try {
            return new XmlNodeVar(content, true);
        } catch (XmlParseException e) {
            throw new RobotRuntimeException("The XML source is invalid.", e);
        } catch (Exception e) {
            throw new RobotRuntimeException("Error occured.", e);
        }
    }

    @Override
    public XmlNode fromString(final String xmlText) {
        try {
            return new XmlNodeVar(xmlText, true);
        } catch (XmlParseException e) {
            throw new RobotRuntimeException("The XML source is invalid." + e.getMessage(), e);
        } catch (Exception e) {
            throw new RobotRuntimeException("Error occured.", e);
        }
    }
}
