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
import nl.xillio.xill.plugins.web.PhantomJSConstruct;
import nl.xillio.xill.plugins.web.data.WebVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;

/**
 * Simulates selection of an item in HTML list
 */
public class SelectConstruct extends PhantomJSConstruct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (element, select) -> process(element, select, webService),
                new Argument("element", ATOMIC),
                new Argument("select", ATOMIC));
    }

    /**
     * @param elementVar input variable (should be of a NODE type) - web element
     * @param selectVar  input boolean variable
     * @param webService The service we're using for accesing the web.
     * @return null variable
     */
    public static MetaExpression process(final MetaExpression elementVar, final MetaExpression selectVar, final WebService webService) {

        if (elementVar.isNull()) {
            return NULL;
        }

        checkNodeType(elementVar);
        boolean select = selectVar.getBooleanValue();
        WebVariable element = getNode(elementVar);
        // Check if we need to click
        if (select != webService.isSelected(element)) {
            webService.click(element);
        }
        return NULL;
    }
}
