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
package nl.xillio.xill.plugins.web.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.web.services.web.HTMLService;

/**
 * Construct for tidying HTML or XHTML.
 *
 * @author Geert Konijnendijk
 */
public class TidyConstruct extends PhantomJSConstruct {

    private static final String SYNTAX_XHTML = "xhtml";
    private static final String SYNTAX_HTML = "html";

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor((html, fullHTML, syntax, prettyPrint) -> process(html, fullHTML, syntax, prettyPrint),
                new Argument("html", ATOMIC),
                new Argument("fullHTML", fromValue(true), ATOMIC),
                new Argument("syntax", fromValue(SYNTAX_XHTML), ATOMIC),
                new Argument("prettyPrint", fromValue(false), ATOMIC));
    }

    private MetaExpression process(MetaExpression html, MetaExpression fullHTML, MetaExpression syntax, MetaExpression prettyPrint) {
        // Get HTML string
        String htmlValue = html.getStringValue();
        // Get whether to parse a body fragment or full html document
        boolean fullHTMLValue = fullHTML.getBooleanValue();
        // Get the syntax string (either html or xml)
        String syntaxValue = syntax.getStringValue();
        // Get whether to pretty print the output
        boolean prettyPrintValue = prettyPrint.getBooleanValue();

        // Call the right method for the combination of values
        if (SYNTAX_XHTML.equals(syntaxValue)) {
            if (fullHTMLValue) {
                return fromValue(getHtmlService().tidyXHTML(htmlValue, prettyPrintValue));
            } else {
                return fromValue(getHtmlService().tidyXHTMLBodyFragment(htmlValue, prettyPrintValue));
            }
        } else if (SYNTAX_HTML.equals(syntaxValue)) {
            if (fullHTMLValue) {
                return fromValue(getHtmlService().tidyHTML(htmlValue, prettyPrintValue));
            } else {
                return fromValue(getHtmlService().tidyHTMLBodyFragment(htmlValue, prettyPrintValue));
            }
        } else {
            throw new OperationFailedException("parse syntax value.", "Syntax value should be either \"" + SYNTAX_HTML + "\" or \"" + SYNTAX_XHTML + "\"");
        }
    }
}
