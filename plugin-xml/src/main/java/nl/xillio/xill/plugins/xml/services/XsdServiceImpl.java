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
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.slf4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

/**
 * This class is the main implementation of the {@link XsdService}
 *
 * @author Zbynek Hochmann
 */

@Singleton
public class XsdServiceImpl implements XsdService, ErrorHandler {
    static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    private static final Logger LOGGER = Log.get();

    private final DocumentBuilderFactory dbf;
    private LinkedList<String> messages = new LinkedList<>();

    /**
     * Constructor of @XsdServiceImpl class
     * It prepares and set up DocumentBuilderFactory
     */
    public XsdServiceImpl() {
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(true);

        try {
            dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        } catch (IllegalArgumentException e) {
            LOGGER.error("JAXP DocumentBuilderFactory attribute not recognized: " + JAXP_SCHEMA_LANGUAGE, e);
        }
    }

    @Override
    public boolean xsdCheck(final Path xmlFile, final Path xsdFile, final Logger logger) {
        messages.clear();

        dbf.setAttribute(JAXP_SCHEMA_SOURCE, xsdFile);
        try (InputStream stream = Files.newInputStream(xmlFile, StandardOpenOption.READ)) {

            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setErrorHandler(this);
            db.parse(stream);
        } catch (IOException e) {
            throw new RobotRuntimeException("XSD check error\n" + e.getMessage(), e);
        } catch (Exception e) {
            logger.warn("XSD check failed\n" + e.getMessage() + (e.getCause() != null ? e.getCause().getMessage() : ""), e);
            return false;
        }

        boolean result = messages.isEmpty();
        if (!result) {
            final String[] text = {"XSD check failed\n"};
            messages.forEach(v -> {
                text[0] += v;
                text[0] += "\n";
            });
            logger.warn(text[0]);
        }
        return result;
    }

    @Override
    public void error(final SAXParseException e) throws SAXException {
        message(e);
    }

    @Override
    public void fatalError(final SAXParseException e) throws SAXException {
        message(e);
    }

    @Override
    public void warning(final SAXParseException e) throws SAXException {
        message(e);
    }

    private void message(final SAXParseException e) {
        messages.add("Line " + e.getLineNumber() + ", Char " + e.getColumnNumber() + ": " + e.getMessage());
    }

}