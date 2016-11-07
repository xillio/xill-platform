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

import nl.xillio.xill.plugins.xml.data.XmlNodeVar;
import nl.xillio.xill.plugins.xml.exceptions.XmlParseException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests to verify the compliancy of the Service with the XPath features, and the underlying SAX library.
 */
public class XpathServiceTest {
    private XmlNodeVar xmlDocumentMeta;
    private XpathServiceImpl xpathService = new XpathServiceImpl();
    private Map<String, String> namespaces = new HashMap<>();

    @BeforeMethod
    public void loadTestXML() throws XmlParseException, URISyntaxException, IOException {
        String xmlText = new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("textXml.xml").toURI())));
        xmlDocumentMeta = new XmlNodeVar(xmlText, true);
        namespaces.put("idc", "http://www.namespaces.null/test");
    }

    @Test
    public void testEmptyResultNodeList() {
        Object result  = xpathService.xpath(xmlDocumentMeta, "//a", namespaces);

        Assert.assertTrue(result instanceof NodeList);
        NodeList resultList = (NodeList) result;
        Assert.assertEquals(resultList.getLength(), 0);
    }

    @Test
    public void testEmptyResultString() {
        Object result  = xpathService.xpath(xmlDocumentMeta, "count(/a/b/c/text())", namespaces);

        Assert.assertTrue(result instanceof String);
        Assert.assertEquals(result, "0");
    }

    @Test
    public void testGetNodeList() {
        Object result = xpathService.xpath(xmlDocumentMeta, "//idc:field", namespaces);

        Assert.assertTrue(result instanceof NodeList);
        NodeList resultNodeList = (NodeList) result;
        Assert.assertEquals(resultNodeList.getLength(), 2);
    }

    @Test
    public void testGetNode() {
        Object result = xpathService.xpath(xmlDocumentMeta, "//idc:field[@name='field1']", namespaces);

        Assert.assertTrue(result instanceof NodeList);
        NodeList resultNodeList = (NodeList) result;
        Assert.assertEquals(resultNodeList.getLength(), 1);
    }

    @Test
    public void testGetString() {
        Object result = xpathService.xpath(xmlDocumentMeta, "//idc:field[@name='field1']/name(@*[1])", namespaces);

        Assert.assertTrue(result instanceof String);
        String resultString = (String) result;
        Assert.assertEquals(resultString, "name");
    }

    @Test
    public void testGetText() {
        Object result;
        // normal text node content
        result = xpathService.xpath(xmlDocumentMeta, "//idc:field[@name='field1']/text()", namespaces);
        assertStringUnaryList(result, "1");

        // text from CDATA
        result = xpathService.xpath(xmlDocumentMeta, "/idc:row/elementWithOnlyCDATA/text()", namespaces);
        assertStringUnaryList(result, "cdata");

        // text from CDATA and multiple text nodes
        result = xpathService.xpath(xmlDocumentMeta, "/idc:row/elementWithManyText/text()", namespaces);
        assertStringUnaryList(result, "text1\n"); // sort of bug in the xpath processor with multiple text nodes and CDATA: text() retrieves only the first. Workaround: extract the node and then get the text()

    }

    private void assertStringUnaryList(Object xpathResult, String expectedValue) {
        Assert.assertTrue(xpathResult instanceof NodeList);
        NodeList resultNodeList = (NodeList) xpathResult;
        Assert.assertEquals(resultNodeList.getLength(), 1);
        Assert.assertTrue(resultNodeList.item(0).getNodeType() == Node.TEXT_NODE || resultNodeList.item(0).getNodeType() == Node.CDATA_SECTION_NODE);
        Assert.assertEquals(resultNodeList.item(0).getTextContent(), expectedValue);
    }


    @Test
    // string list
    public void testGetStringList() {
        Object result = xpathService.xpath(xmlDocumentMeta, "/idc:row/elementWithAttributes/@*", namespaces);
        Assert.assertTrue(result instanceof NodeList);
        NodeList resultNodeList = (NodeList)result;
        Assert.assertEquals(resultNodeList.getLength(), 2);
        Assert.assertEquals(resultNodeList.item(0).getNodeValue(), "1");
        Assert.assertEquals(resultNodeList.item(1).getNodeValue(), "2");
    }
}
