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

import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.web.data.WebVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;

import java.util.ArrayList;
import java.util.List;

/**
 * Select web element(s) on the page according to provided CSS Path selector
 */
public class CSSPathConstruct extends PhantomJSConstruct {

    @Override
    public String getName() {
        return "cssPath";
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (element, csspath) -> process(element, csspath),
                new Argument("element", ATOMIC),
                new Argument("cssPath", ATOMIC));
    }

    /**
     * @param elementVar input variable (should be of a NODE or PAGE type)
     * @param cssPathVar string variable specifying CSS Path selector
     * @return NODE variable or list of NODE variables or null variable (according to count of selected web elements - more/1/0)
     */
    private MetaExpression process(final MetaExpression elementVar, final MetaExpression cssPathVar) {

        String query = cssPathVar.getStringValue();

        if (elementVar.isNull()) {
            return NULL;
        }
        if (isNodeAndNotPage(elementVar)) {
            return processSELNode(getNode(elementVar), query, getWebService());
        } else {
            return processSELNode(getPage(elementVar), query, getWebService());
        }
    }

    private MetaExpression processSELNode(final WebVariable node, final String selector, final WebService webService) {
        List<WebVariable> results = webService.findElementsWithCssPath(node, selector);

        if (results.isEmpty()) {
            return NULL;
        } else if (results.size() == 1) {
            return createNode(node, results.get(0), webService);
        } else {
            ArrayList<MetaExpression> list = new ArrayList<MetaExpression>();

            for (WebVariable element : results) {
                list.add(createNode(node, element, webService));
            }

            return ExpressionBuilderHelper.fromValue(list);
        }

    }
}
