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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Entities;

/**
 * Implementation of {@link HTMLService}.
 *
 * @author Geert Konijnendijk
 */
public class HTMLServiceImpl implements HTMLService {

    @Override
    public String tidyHTML(String html, boolean prettyPrint) {
        Document document = parse(html);
        setOutputOptions(document, Syntax.html, prettyPrint);
        return document.outerHtml();
    }

    @Override
    public String tidyXHTML(String html, boolean prettyPrint) {
        Document document = parse(html);
        setOutputOptions(document, Syntax.xml, prettyPrint);
        return document.outerHtml();
    }

    @Override
    public String tidyHTMLBodyFragment(String bodyFragment, boolean prettyPrint) {
        Document document = parseBodyFragment(bodyFragment);
        setOutputOptions(document, Syntax.html, prettyPrint);
        return document.body().html();
    }

    @Override
    public String tidyXHTMLBodyFragment(String bodyFragment, boolean prettyPrint) {
        Document document = parseBodyFragment(bodyFragment);
        setOutputOptions(document, Syntax.xml, prettyPrint);
        return document.body().html();
    }

    /**
     * Parse a string as a full HTML document so html and body tags will be included in the output.
     *
     * @param html The HTML to parse
     * @return The parsed document
     */
    Document parse(String html) {
        return Jsoup.parse(html);
    }

    /**
     * Parse a string as a fragment from an html body element.
     *
     * @param bodyFragment The html fragment to parse
     * @return The parsed document
     */
    Document parseBodyFragment(String bodyFragment) {
        return Jsoup.parseBodyFragment(bodyFragment);
    }

    /**
     * Set options for the document to generate correct output
     *
     * @param document    The document set the options for
     * @param syntax      Whether to parse as HTML or XHTML
     * @param prettyPrint If true, output HTML/XHTML is formatted nicely
     */
    private void setOutputOptions(Document document, Syntax syntax, boolean prettyPrint) {
        Document.OutputSettings outputSettings = document.outputSettings();
        // XHTML needs less characters escaped than HTML
        if (syntax == Syntax.xml) {
            outputSettings.escapeMode(Entities.EscapeMode.xhtml);
        }
        outputSettings.syntax(syntax);
        outputSettings.prettyPrint(prettyPrint);
    }
}
