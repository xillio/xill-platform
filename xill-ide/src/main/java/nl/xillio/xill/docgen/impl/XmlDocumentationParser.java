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
package nl.xillio.xill.docgen.impl;

import me.biesaart.utils.Log;
import nl.xillio.xill.docgen.DocumentationEntity;
import nl.xillio.xill.docgen.DocumentationParser;
import nl.xillio.xill.docgen.data.Example;
import nl.xillio.xill.docgen.data.ExampleNode;
import nl.xillio.xill.docgen.data.Reference;
import nl.xillio.xill.docgen.exceptions.ParsingException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * The class represents a parser that will parse xml files into {@link DocumentationEntity}
 *
 * @author Thomas Biesaart
 * @author Ivor van der Hoog
 * @since 12-8-2015
 */
public class XmlDocumentationParser implements DocumentationParser {
    private static final Logger LOGGER = Log.get();
    private final XPathFactory xpathFactory;
    private final DocumentBuilderFactory documentBuilderFactory;
    private XPathExpression descriptionXPathQuery;
    private XPathExpression deprecateDescriptionXPath;
    private XPathExpression tagXPathQuery;
    private XPathExpression exampleNodesXPathQuery;
    private XPathExpression referenceXPathQuery;
    private static PegDownProcessor markdownProcessor = new PegDownProcessor(Extensions.ALL);

    /**
     * The constructor for the parser when we hand it a factory.
     *
     * @param xpathFactory           The {@link XPathFactory} we want the parser to use.
     * @param documentBuilderFactory the documentfactory that should be used.
     */
    public XmlDocumentationParser(final XPathFactory xpathFactory, final DocumentBuilderFactory documentBuilderFactory) {
        this.xpathFactory = xpathFactory;
        this.documentBuilderFactory = documentBuilderFactory;
        this.documentBuilderFactory.setCoalescing(true);
        try {
            buildQueries();
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("Failed to build xPath queries", e);
        }
    }

    /**
     * Instantiate a new XmlDocumentationParser using the default factories
     */
    public XmlDocumentationParser() {
        this(XPathFactory.newInstance(), DocumentBuilderFactory.newInstance());
    }

    private void buildQueries() throws XPathExpressionException {
        XPath xPath = xpathFactory.newXPath();
        descriptionXPathQuery = xPath.compile("/function/description/text()");
        deprecateDescriptionXPath = xPath.compile("/function/deprecated/text()");
        tagXPathQuery = xPath.compile("/function/tags");
        exampleNodesXPathQuery = xPath.compile("/function/examples/example");
        referenceXPathQuery = xPath.compile("/function/references/reference");
    }

    @Override
    public DocumentationEntity parse(final URL resource, final String identity) throws ParsingException {
        try {
            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(openStream(resource));

            return doParse(doc, identity);
        } catch (IllegalArgumentException | IOException e) {
            throw new ParsingException("Failed to access XML for: " + identity, e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new ParsingException("Failed to parse XML for: " + identity, e);
        }
    }

    InputStream openStream(final URL url) throws IOException {
        return url.openStream();
    }

    DocumentationEntity doParse(final Document doc, final String identity) throws ParsingException {

        ConstructDocumentationEntity construct = new ConstructDocumentationEntity(identity);

        construct.setDescription(parseDescription(doc));
        construct.setDeprecateDescription(parseDeprecateDescription(doc));
        construct.setExamples(parseExamples(doc));
        construct.setReferences(parseReferences(doc));
        construct.setSearchTags(parseSearchTags(doc));

        return construct;
    }

    String parseDescription(final Document doc) throws ParsingException {
        try {
            return parseStringFromXPath(doc, descriptionXPathQuery);
        } catch (XPathExpressionException e) {
            throw new ParsingException("Failed to parse description", e);
        }
    }


    String parseDeprecateDescription(final Document doc) throws ParsingException {
        try {
            return parseStringFromXPath(doc, deprecateDescriptionXPath);
        } catch (XPathExpressionException e) {
            throw new ParsingException("Failed to parse deprecate description", e);
        }
    }

    /**
     * Query doc using the given {@link XPathExpression} and parse the resulting string as markdown.
     * @param doc The document to use
     * @param query An XPath query that should result in a string
     * @return Parsed markdown as HTML
     * @throws XPathExpressionException When the XPath query fails
     */
    private String parseStringFromXPath(Document doc, XPathExpression query) throws XPathExpressionException {
        return markdownProcessor.markdownToHtml((String) query.evaluate(doc, XPathConstants.STRING));
    }

    String getAttributeOrNull(final String name, final Node node) {
        NamedNodeMap attributes = node.getAttributes();
        Node attribute = attributes.getNamedItem(name);
        if (attribute == null) {
            return null;
        }

        return attribute.getNodeValue();
    }

    List<Example> parseExamples(final Document doc) throws ParsingException {
        List<Example> examples = new ArrayList<>();
        NodeList exampleNodes;
        try {
            exampleNodes = (NodeList) exampleNodesXPathQuery.evaluate(doc, XPathConstants.NODESET);

            for (int t = 0; t < exampleNodes.getLength(); ++t) {
                examples.add(parseExample(exampleNodes.item(t)));
            }
        } catch (XPathExpressionException | NullPointerException e) {
            throw new ParsingException("Failed to parse examples", e);
        }

        return examples;
    }

    Example parseExample(final Node node) {
        // Get the example title
        Example example = new Example(getAttributeOrNull("title", node));
        NodeList exampleContent = node.getChildNodes();
        for (int t = 0; t < exampleContent.getLength(); ++t) {
            Node item = exampleContent.item(t);
            if (item.getNodeName() != null && !item.getNodeName().startsWith("#")) {
                example.addContent(
                        new ExampleNode(exampleContent.item(t).getNodeName(),
                                StringEscapeUtils.escapeHtml3(exampleContent.item(t).getTextContent())));
            }
        }
        return example;
    }

    List<Reference> parseReferences(final Document doc) throws ParsingException {
        List<Reference> references = new ArrayList<>();
        NodeList exampleNodes;
        try {
            exampleNodes = (NodeList) referenceXPathQuery.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException | IllegalArgumentException | NullPointerException e) {
            throw new ParsingException("Failed to parse references", e);
        }

        for (int t = 0; t < exampleNodes.getLength(); ++t) {
            try {
                references.add(parseReference(exampleNodes.item(t)));
            } catch (ParsingException | NullPointerException e) {
                LOGGER.error("Failed to parse reference", e);
            }
        }

        return references;
    }

    Reference parseReference(final Node node) throws ParsingException {
        String[] reference = node.getTextContent().trim().split("\\.");
        if (reference.length == 0) {
            throw new ParsingException("Failed to parse reference because no content was found");
        }

        String packageName = null;
        String constructName = reference[reference.length - 1];
        if (reference.length > 1) {
            packageName = reference[0];
        }

        return new Reference(packageName, constructName);
    }

    Set<String> parseSearchTags(final Document doc) throws ParsingException {
        String[] searchTags = new String[0];
        try {
            Node searchTagNode = (Node) tagXPathQuery.evaluate(doc, XPathConstants.NODE);
            if (searchTagNode != null) {
                searchTags = searchTagNode.getTextContent().replaceAll("\\s", "").split(",");
            }
        } catch (XPathExpressionException | NullPointerException e) {
            throw new ParsingException("Failed to parse tags", e);
        }

        return new HashSet<>(Arrays.asList(searchTags));
    }
}
