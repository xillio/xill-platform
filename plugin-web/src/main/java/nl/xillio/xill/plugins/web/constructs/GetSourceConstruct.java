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
import nl.xillio.xill.plugins.web.services.web.WebService;

/**
 * Gets the HTML content of the web page.
 */
public class GetSourceConstruct extends PhantomJSConstruct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                page -> process(page, getWebService()),
                new Argument("page", ATOMIC));
    }

    /**
     * @param pageVar    input PAGE type variable
     * @param webService the service we're using.
     * @return HTML content of the web page
     */
    public static MetaExpression process(final MetaExpression pageVar, final WebService webService) {
        assertNotNull(pageVar, "element");

        checkPageType(pageVar);
        return fromValue(webService.getSource(getPage(pageVar)));
    }
}
