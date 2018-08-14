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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.*;

public class XsdServiceImplTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(XsdServiceImplTest.class);
    private XsdServiceImpl xsdService;
    private Path xsdFile;
    private Path xmlValid;
    private Path xmlInvalid;


    @BeforeClass
    public void prepare() throws URISyntaxException {
        xsdFile = Paths.get(getClass().getClassLoader().getResource("xsdValidation.xsd").toURI());
        xmlValid = Paths.get(getClass().getClassLoader().getResource("xsdValidationValid.xml").toURI());
        xmlInvalid = Paths.get(getClass().getClassLoader().getResource("xsdValidationInvalid.xml").toURI());
    }

    @BeforeMethod
    public void setUp() {
        xsdService = new XsdServiceImpl();
    }

    @Test
    public void testXsdCheckValid() {
        assertTrue(xsdService.xsdCheck(xmlValid, xsdFile, LOGGER));
    }

    @Test
    public void testXsdCheckGetIssueListValid() {
        assertTrue(xsdService.xsdCheckGetIssueList(xmlValid, xsdFile, LOGGER).isEmpty());
    }

    @Test
    public void testXsdCheckInvalid() {
        assertFalse(xsdService.xsdCheck(xmlInvalid, xsdFile, LOGGER));
    }

    @Test
    public void testXsdCheckGetIssueListInvalid() {
        assertEquals(xsdService.xsdCheckGetIssueList(xmlInvalid, xsdFile, LOGGER).size(), 2);
    }
}
