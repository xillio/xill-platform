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
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.web.PhantomJSConstruct;
import nl.xillio.xill.plugins.web.data.WebVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;

import java.util.List;

/**
 * Gets the text content from provided web element.
 */
public class GetTextConstruct extends PhantomJSConstruct {

    @Inject
    private WebService webService;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                element -> process(element, webService),
                new Argument("element", LIST, ATOMIC));
    }

    /**
     * @param elementVar input variable (should be of a NODE type or list of NODE type variables)
     * @param webService the service we're using.
     * @return string variable that contains the text(s) of the provided web element(s)
     */
    public static MetaExpression process(final MetaExpression elementVar, final WebService webService) {
        assertNotNull(elementVar, "element");

        String output = "";
        if (elementVar.getType() == LIST) {
            @SuppressWarnings("unchecked")
            List<MetaExpression> list = (List<MetaExpression>) elementVar.getValue();
            for (MetaExpression item : list) {
                output += processItem(item, webService);
            }
        } else {
            output = processItem(elementVar, webService);
        }

        return fromValue(output);
    }

    private static String processItem(final MetaExpression var, final WebService webService) {
        WebVariable element;

        String text;

        if (isNodeAndNotPage(var)) {
            element = getNode(var);
            if ("input".equals(webService.getTagName(element)) || "textarea".equals(webService.getTagName(element))) {
                text = webService.getAttribute(element, "value");
            } else {
                text = webService.getText(element);
            }
        } else {
            element = getPage(var);
            text = webService.getText(element);
        }
        return text;
    }
}
