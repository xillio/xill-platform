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
package nl.xillio.xill.util.settings;

import me.biesaart.utils.Log;
import org.apache.commons.io.FileUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is XML file content handler implementation
 * It uses XML structure to store settings.
 * <p>
 * Root level is &lt;settings&gt;.
 * The first level is "category" - tag having the category name (e.g. &lt;Layout&gt;).
 * Each category can contains items (tags &lt;item&gt;).
 * Each item can have key or not - if has then it's attribute of item tag (e.g. &lt;item key='name'&gt;).
 * Each item contains one or more values - tag name means the name of value and the tag text is value itself.
 * <p>
 * The values encrypting is done out of the content handler and it's transparent for content handler.
 *
 * @author Zbynek Hochmann
 */
public class ContentHandlerImpl implements ContentHandler {

    private static final Logger LOGGER = Log.get();

    private File storage;
    private boolean manualCommit = false;
    private Document document;
    private TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private XPathFactory xpathFactory = XPathFactory.newInstance();

    private Object lock = new Object(); // This lock object is used to prevent manipulation with XML content for more than one thread concurrently

    private static String DEFAULTPW = "6WyMNf99H32Qn3ofZ32rxVNTXcd8sA6b";
    private static StandardPBEStringEncryptor encryptor;

    static {
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(DEFAULTPW);
        encryptor.initialize();
    }

    /**
     * Constructor that sets up the target XML file.
     *
     * @param file target XML file
     */
    public ContentHandlerImpl(final File file) {
        this.storage = file;
    }

    @Override
    public void init() throws IOException {
        // Open existing XML file with settings
        // If file does not exist it will create new one with the basic structure (i.e. just root node)
        FileInputStream settingsStream=null;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            if (!storage.exists()) {
                this.document = docBuilder.newDocument();
                this.createInnerStructure();
                this.save(true);
            } else {
                settingsStream = FileUtils.openInputStream(storage);
                this.document = docBuilder.parse(settingsStream);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IOException("Cannot open or parse file.", e);
        }
        finally {
            if (settingsStream!=null){
                settingsStream.close();
            }
        }
    }

    private void createInnerStructure() {
        Element root = this.document.createElement("settings");
        this.document.appendChild(root);
    }

    /**
     * Saves the current XML content to file
     *
     * @param force false means that save is done only if manualCommit = false, true means save is done always
     * @throws Exception when error occurs during saving to file
     */
    private void save(boolean force) {
        if ((!force) && (this.manualCommit)) {
            return;
        }
        synchronized (lock) {
            try {
                FileUtils.touch(storage);
                Transformer transformer = this.transformerFactory.newTransformer();
                DOMSource source = new DOMSource(this.document);
                StreamResult result = new StreamResult(this.storage);
                transformer.transform(source, result);
            } catch (IOException | TransformerException e) {
                LOGGER.error("Error saving file.", e);
            }
        }
    }

    @Override
    public Map<String, Object> get(final String category, final String keyValue) throws IOException {
        synchronized (lock) {
            // Tries to iterate all items in given category
            String path = String.format("%1$s/item", category);
            Object result = null;
            try {
                result = xpathFactory.newXPath().compile(path).evaluate(this.document.getFirstChild(), XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                throw new IllegalArgumentException("Given category contains illegal characters.", e);
            }
            if (result == null) {
                return null;
            }
            if (result instanceof NodeList) {
                HashMap<String, Object> map = new HashMap<>();
                NodeList list = (NodeList) result;
                for (int i = 0; i < list.getLength(); i++) {
                    Node node = list.item(i);
                    Node attr = node.getAttributes().getNamedItem("key");
                    if (attr != null) { // Deal with the items with key only
                        if (this.keyValueMatch(node, attr.getNodeValue(), keyValue)) {
                            // Key value matches (item found) so iterate all item values and put them to map
                            Node child = node.getFirstChild();
                            while (child != null) {
                                map.put(child.getNodeName(), child.getTextContent());
                                child = child.getNextSibling();
                            }
                            break;
                        }
                    }
                }
                return map;
            }
        }
        throw new IOException("Invalid content structure of settings file!");
    }

    private boolean keyValueMatch(final Node item, final String keyName, final String keyValue) {
        // Returns true if the key matches
        Node child = item.getFirstChild();
        while (child != null) { // Iterates all item values and looks for tag name=keyName and tag text=keyValue
            if (child.getNodeName().equals(keyName)) {
                if (child.getTextContent().equals(keyValue)) {
                    return true;
                }
            }
            child = child.getNextSibling();
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> getAll(final String category) throws IOException {
        synchronized (lock) {
            String path = String.format("%1$s/item", category);
            Object result = null;
            try {
                result = xpathFactory.newXPath().compile(path).evaluate(this.document.getFirstChild(), XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                throw new IllegalArgumentException("Given category contains illegal characters.", e);
            }
            if (result == null) {
                return null;
            }
            if (result instanceof NodeList) {
                LinkedList<Map<String, Object>> output = new LinkedList<>();

                NodeList nodeList = (NodeList) result;
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node parentNode = nodeList.item(i);
                    HashMap<String, Object> map = new HashMap<>();
                    Node node = parentNode.getFirstChild();
                    while (node != null) {
                        map.put(node.getNodeName(), node.getTextContent());
                        node = node.getNextSibling();
                    }
                    output.add(map);
                }
                return output;
            }
        }
        throw new IOException("Invalid content structure of settings file!");
    }

    @Override
    public boolean set(final String category, final Map<String, Object> itemContent, final String keyName, final String keyValue) {
        boolean created = false;
        synchronized (lock) {
            Node categoryNode = null;
            try {
                categoryNode = this.getCategoryNode(category);
            } catch (XPathExpressionException e) {
                throw new IllegalArgumentException("Given category contains illegal characters.", e);
            }
            Element itemNode[] = {null};
            if (keyName != null) {
                try {
                    itemNode[0] = this.getItemNode(categoryNode, keyName, keyValue);
                } catch (XPathExpressionException e) {
                    throw new IllegalArgumentException("Given key or value contains illegal characters.", e);
                }
            }
            if (itemNode[0] == null) {
                // creates new item node
                created = true;
                itemNode[0] = this.document.createElement("item");
                categoryNode.appendChild(itemNode[0]);
                if (keyName != null) {
                    itemNode[0].setAttribute("key", keyName);
                }
            }

            itemContent.forEach((k, v) -> set(itemNode[0], k, v));

            this.save(false);
        }
        return created;
    }

    @Override
    public boolean exist(final String category, final String keyName, final String keyValue) {
        String path = String.format("%1$s/item[@key='%2$s'][%2$s/text()='%3$s']", category, keyName, keyValue);
        synchronized (lock) {
            Object result = null;
            try {
                result = xpathFactory.newXPath().compile(path).evaluate(this.document.getFirstChild(), XPathConstants.NODE);
            } catch (XPathExpressionException e) {
                throw new IllegalArgumentException("Given category contains illegal characters.", e);
            }
            return (result != null);
        }
    }

    @Override
    public boolean delete(final String category, final String keyName, final String keyValue) {
        synchronized (lock) {
            Node categoryNode = null;
            try {
                categoryNode = this.getCategoryNode(category);
            } catch (XPathExpressionException e) {
                throw new IllegalArgumentException("Given category contains illegal characters.", e);
            }
            Element itemNode = null;
            try {
                itemNode = this.getItemNode(categoryNode, keyName, keyValue);
            } catch (XPathExpressionException e) {
                throw new IllegalArgumentException("Given key or value contains illegal characters.", e);
            }
            if (itemNode == null) {
                return false;
            }
            categoryNode.removeChild(itemNode);

            this.save(false);
        }

        return true;
    }

    /**
     * Sets the value
     * It iterates all item values and tries to find existing (according to it's name - nodeName)
     * - if found the new value is overwritten, if not found new item value is created.
     *
     * @param itemNode  The item node (<item>)
     * @param nodeName  The name of value
     * @param nodeValue The value itself to set
     */
    private void set(Element itemNode, final String nodeName, final Object nodeValue) {
        String path = String.format("%1$s", nodeName);
        Element node;
        try {
            node = (Element) xpathFactory.newXPath().compile(path).evaluate(itemNode, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            LOGGER.error(e.getMessage(), e);
            return;
        }

        if (node == null) {
            // create new node
            node = this.document.createElement(nodeName);
            itemNode.appendChild(node);
        }

        String nodeValueStr = "";
        if (nodeValue != null) {
            nodeValueStr = nodeValue.toString();
        }

        Node text = node.getFirstChild();
        if (text == null) {
            // set new node text value
            node.appendChild(this.document.createTextNode(nodeValueStr));
        } else {
            // update existing node text value
            text.setNodeValue(nodeValueStr);
        }
    }

    /**
     * Supportive method for encrypting the value
     *
     * @param value Input value in open text
     * @return Encrypted value
     */
    public static synchronized String encrypt(final String value) {
        return encryptor.encrypt(value);
    }

    /**
     * Supportive method for decrypting the value
     *
     * @param value Encrypted value
     * @return Decrypted value
     */
    public static synchronized String decrypt(final String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return encryptor.decrypt(value);
    }

    private Element getItemNode(Node categoryNode, final String keyName, final String keyValue) throws XPathExpressionException {
        // find the item having key attribute=keyName and having tag that has name=keyName and text value=keyValue
        String path = String.format("item[@key='%1$s'][%1$s/text()='%2$s']", keyName, keyValue);
        return (Element) xpathFactory.newXPath().compile(path).evaluate(categoryNode, XPathConstants.NODE);
    }

    private Node getCategoryNode(final String category) throws XPathExpressionException {
        // Tries to find given category node in XML
        // If not found, the new node is created so it always returns the Node (never null)
        String path = String.format("/settings/%1$s", category);
        Node root = this.document.getFirstChild();
        Object o = xpathFactory.newXPath().compile(path).evaluate(root, XPathConstants.NODE);
        if (o == null) {
            Node categoryNode = this.document.createElement(category);
            root.appendChild(categoryNode);
            return categoryNode;
        } else {
            return (Node) o;
        }
    }

    @Override
    public boolean setManualCommit(boolean manual) {
        this.manualCommit = manual;
        return true;
    }

    @Override
    public boolean commit() {
        this.save(true);
        return true;
    }
}
