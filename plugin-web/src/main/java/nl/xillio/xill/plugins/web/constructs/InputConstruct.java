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
 * Simulates key presses on provided web element (i.e. clear and write text)
 */
public class InputConstruct extends PhantomJSConstruct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (element, text) -> process(element, text, webService),
                new Argument("element", ATOMIC),
                new Argument("text", ATOMIC));
    }

    /**
     * @param elementVar input variable (should be of a NODE type) - web element
     * @param textVar    input string variable - text to be written to web element
     * @param webService The webService we're using.
     * @return null variable
     */
    public static MetaExpression process(final MetaExpression elementVar, final MetaExpression textVar, final WebService webService) {

        if (elementVar.isNull()) {
            return NULL;
        }

        checkNodeType(elementVar);
        // else

        String text = textVar.getStringValue();

        WebVariable element = getNode(elementVar);
        webService.clear(element);
        webService.sendKeys(element, text);

        return NULL;
    }

}
