package nl.xillio.xill.plugins.xml.services;

import com.google.common.collect.Iterables;
import nl.xillio.xill.plugins.xml.data.XmlNodeVar;
import nl.xillio.xill.plugins.xml.exceptions.XmlParseException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
    public void testEmptyResult() {
        List<Object> result  = xpathService.xpath(xmlDocumentMeta, "/a/b/c", namespaces);

        Assert.assertTrue(result.size() == 0);
    }

    @Test
    public void testGetNodeList() {
        List<Object> result = xpathService.xpath(xmlDocumentMeta, "//idc:field", namespaces);

        Assert.assertEquals(result.size(), 2);
    }

    @Test
    public void testGetNode() {
        List<Object> result = xpathService.xpath(xmlDocumentMeta, "//idc:field[@name='field1']", namespaces);

        Assert.assertEquals(result.size(), 1);
    }

    @Test
    public void testGetString() {
        List<Object> result = xpathService.xpath(xmlDocumentMeta, "//idc:field[@name='field1']/name(@*[1])", namespaces);

        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.get(0) instanceof String);
        Assert.assertEquals(result.get(0), "name");
    }

    @Test
    // normal text node content
    public void testGetText() {
        List<Object> result = xpathService.xpath(xmlDocumentMeta, "//idc:field[@name='field1']/text()", namespaces);

        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.get(0) instanceof String);
        Assert.assertEquals(result.get(0), "1");
    }

    @Test
    // function producing text
    public void testGetText2() {
        List<Object> result = xpathService.xpath(xmlDocumentMeta, "//idc:field[@name='field1']/concat(text(), 'x')", namespaces);

        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.get(0) instanceof String);
        Assert.assertEquals(result.get(0), "1x");
    }

    @Test
    // text from CDATA
    public void testGetText3() {
        List<Object> result = xpathService.xpath(xmlDocumentMeta, "/idc:row/elementWithOnlyCDATA/text()", namespaces);

        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.get(0) instanceof String);
        Assert.assertEquals(result.get(0), "cdata");
    }

    @Test
    // text from CDATA and multiple text nodes
    public void testGetText4() {
        List<Object> result = xpathService.xpath(xmlDocumentMeta, "/idc:row/elementWithManyText/text()", namespaces);

        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.get(0) instanceof String);
        Assert.assertEquals(result.get(0), "text1\ntext2\ntext3");
    }

    @Test
    // string list
    public void testGetStringList() {
        List<Object> result = xpathService.xpath(xmlDocumentMeta, "/idc:row/elementWithAttributes/@*", namespaces);
        List<Object> expected = Arrays.asList(new String[] {"1", "2"});

        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(Iterables.elementsEqual(result, expected));
    }
}
