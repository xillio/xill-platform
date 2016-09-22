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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.web.data.WebVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;

import java.util.ArrayList;
import java.util.List;

/**
 * Select web element(s) on the page according to provided XPath selector
 */
public class XPathConstruct extends PhantomJSConstruct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (element,xPath) -> process(element,xPath),
                new Argument("element", ATOMIC),
                new Argument("xPath", ATOMIC));
    }

    /**
     * @param elementVar input variable (should be of a NODE or PAGE type)
     * @param xPathVar   string variable specifying XPath selector
     * @return NODE variable or list of NODE variables or null variable (according to count of selected web elements - more/1/0)
     */
    private MetaExpression process(final MetaExpression elementVar, final MetaExpression xPathVar) {

        String query = xPathVar.getStringValue();

        if (elementVar.isNull()) {
            return NULL;
        }

        if (isNodeAndNotPage(elementVar)) {
            return processSELNode(getNode(elementVar), query);
        } else {
            return processSELNode(getPage(elementVar), query);
        }
    }

    private MetaExpression processSELNode(final WebVariable driver, final String query) {
        String cleanedQuery = query;
        boolean textQuery = query.endsWith("/text()");
        boolean attributeQuery = query.matches("^.*(@[:A-z_][-:\\w\\.]*)$");
        String attribute = null;

        if (textQuery) {
            cleanedQuery = stripTextTag(query);
        } else if (attributeQuery) {
            attribute = getAttribute(query);
            cleanedQuery = stripAttributeQuery(query);
        }

        List<WebVariable> results = getWebService().findElementsWithXpath(driver, cleanedQuery);

        if (results.isEmpty()) {
            return NULL;
        } else if (results.size() == 1) {
            return parseSELVariable(driver, results.get(0), textQuery, attribute);
        } else {
            ArrayList<MetaExpression> list = new ArrayList<>();

            for (WebVariable he : results) {
                list.add(parseSELVariable(driver, he, textQuery, attribute));
            }

            return fromValue(list);
        }
    }

    /**
     * Strips the query from the /text()
     *
     * @param query The query we need to strip.
     * @throws OperationFailedException if the index is out of bounds
     * @return The stripped query
     */
    private String stripTextTag(final String query) {
        try {
            return query.substring(0, query.length() - 7);
        } catch (IndexOutOfBoundsException e) {
            throw new OperationFailedException("strip the text Tag", "An index was out of bounds when stripping: " + query, e);
        }
    }

    /**
     * Gets the attribute part of the xpath
     *
     * @param xpath The xpath we want to extract the attribute from.
     * @throws OperationFailedException if the index is out of bounds
     * @return The attribute name.
     */
    private String getAttribute(final String xpath) {
        try {
            return xpath.substring(xpath.lastIndexOf('@') + 1);
        } catch (IndexOutOfBoundsException e) {
            throw new OperationFailedException("get attribute from xPath", "An index was out of bounds when stripping: " + xpath + " when extracting the attribute.", e);
        }
    }

    /**
     * Strips an attribute xpath till its core.
     *
     * @param query The query we want to strip.
     * @throws OperationFailedException if the index is out of bounds
     * @return The stripped query.
     */
    private String stripAttributeQuery(final String query) {
        try {
            return query.substring(0, query.lastIndexOf('/'));
        } catch (IndexOutOfBoundsException e) {
            throw new OperationFailedException("strip an attribute xPath.", "An index was out of bounds when stripping: " + query + " when indexing \\.", e);
        }
    }

    private MetaExpression parseSELVariable(final WebVariable driver, final WebVariable element, final boolean textquery, final String attribute) {
        if (textquery) {
            return fromValue(getWebService().getAttribute(element, "innerHTML"));
        } else if (attribute != null) {
            String val = getWebService().getAttribute(element, attribute);
            if (val == null) {
                return NULL;
            }
            return fromValue(val);
        } else {
            return createNode(driver, element, getWebService());
        }
    }
}
