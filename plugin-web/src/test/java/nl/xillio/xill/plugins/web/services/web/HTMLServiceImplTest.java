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
package nl.xillio.xill.plugins.web.services.web;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

/**
 * Tests for {@link HTMLServiceImpl}.
 *
 * @author Geert Konijnendijk
 */
public class HTMLServiceImplTest {

    private static final String TEST_HTML = "<p>test",
            FULL_HTML_OUTPUT = "<html><head></head><body><p>test</p></body></html>",
            FRAGMENT_HTML_OUTPUT = "<p>test</p>";

    private static final boolean PRETTY_PRINT = true;

    /**
     * Test a call to {@link HTMLServiceImpl#tidyHTML(String, boolean)}
     */
    @Test
    public void testTidyHTML() {
        // Mock
        Document.OutputSettings outputSettings = mockOutputSettings();
        Document document = mockDocument(outputSettings);
        HTMLServiceImpl service = mockHTMLServiceImpl(document);

        // Run
        String output = service.tidyHTML(TEST_HTML, PRETTY_PRINT);

        // Verify
        verify(service).parse(eq(TEST_HTML));
        verify(outputSettings).syntax(eq(Document.OutputSettings.Syntax.html));
        verify(outputSettings).prettyPrint(eq(PRETTY_PRINT));

        // Assert
        assertSame(output, FULL_HTML_OUTPUT);
    }

    /**
     * Test a call to {@link HTMLServiceImpl#tidyXHTML(String, boolean)}
     */
    @Test
    public void testTidyXHTML() {
        // Mock
        Document.OutputSettings outputSettings = mockOutputSettings();
        Document document = mockDocument(outputSettings);
        HTMLServiceImpl service = mockHTMLServiceImpl(document);

        // Run
        String output = service.tidyXHTML(TEST_HTML, PRETTY_PRINT);

        // Verify
        verify(service).parse(eq(TEST_HTML));
        verify(outputSettings).syntax(eq(Document.OutputSettings.Syntax.xml));
        verify(outputSettings).prettyPrint(eq(PRETTY_PRINT));

        // Assert
        assertSame(output, FULL_HTML_OUTPUT);
    }


    /**
     * Test a call to {@link HTMLServiceImpl#tidyHTMLBodyFragment(String, boolean)}
     */
    @Test
    public void testTidyHTMLBodyFragment() {
        // Mock
        Document.OutputSettings outputSettings = mockOutputSettings();
        Document document = mockDocument(outputSettings);
        HTMLServiceImpl service = mockHTMLServiceImpl(document);

        // Run
        String output = service.tidyHTMLBodyFragment(TEST_HTML, PRETTY_PRINT);

        // Verify
        verify(service).parseBodyFragment(eq(TEST_HTML));
        verify(outputSettings).syntax(eq(Document.OutputSettings.Syntax.html));
        verify(outputSettings).prettyPrint(eq(PRETTY_PRINT));

        // Assert
        assertSame(output, FRAGMENT_HTML_OUTPUT);
    }

    /**
     * Test a call to {@link HTMLServiceImpl#tidyXHTMLBodyFragment(String, boolean)}
     */
    @Test
    public void testTidyXHTMLBodyFragment() {
        // Mock
        Document.OutputSettings outputSettings = mockOutputSettings();
        Document document = mockDocument(outputSettings);
        HTMLServiceImpl service = mockHTMLServiceImpl(document);

        // Run
        String output = service.tidyXHTMLBodyFragment(TEST_HTML, PRETTY_PRINT);

        // Verify
        verify(service).parseBodyFragment(eq(TEST_HTML));
        verify(outputSettings).syntax(eq(Document.OutputSettings.Syntax.xml));
        verify(outputSettings).prettyPrint(eq(PRETTY_PRINT));

        // Assert
        assertSame(output, FRAGMENT_HTML_OUTPUT);
    }

    /**
     * @param document The document to be returned when {@link HTMLServiceImpl#parse(String)} or {@link HTMLServiceImpl#parseBodyFragment(String)} are called
     * @return A mocked {@link HTMLServiceImpl} on which {@link HTMLServiceImpl#parse(String)} and {@link HTMLServiceImpl#parseBodyFragment(String)} can be called
     */
    private HTMLServiceImpl mockHTMLServiceImpl(Document document) {
        HTMLServiceImpl service = spy(HTMLServiceImpl.class);

        doReturn(document).when(service).parse(any());
        doReturn(document).when(service).parseBodyFragment(any());
        return service;
    }

    /**
     * @param outputSettings The {@link org.jsoup.nodes.Document.OutputSettings} to return from {@link Document#outputSettings()}
     * @return A mocked {@link Document} on which {@link Document#outputSettings()}, {@link Document#outerHtml()} and {@link Document#body()} can be called
     */
    private Document mockDocument(Document.OutputSettings outputSettings) {
        Document document = mock(Document.class);
        when(document.outputSettings()).thenReturn(outputSettings);
        when(document.outerHtml()).thenReturn(FULL_HTML_OUTPUT);
        Element body = mock(Element.class);
        when(document.body()).thenReturn(body);
        when(body.html()).thenReturn(FRAGMENT_HTML_OUTPUT);
        return document;
    }

    /**
     * @return Mock OutputSettings
     */
    private Document.OutputSettings mockOutputSettings() {
        return mock(Document.OutputSettings.class);
    }


}
