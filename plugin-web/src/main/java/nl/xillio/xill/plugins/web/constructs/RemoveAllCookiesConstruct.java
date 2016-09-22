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
import nl.xillio.xill.plugins.web.data.WebVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;

/**
 * Removes all cookies from a currently loaded page context.
 */
public class RemoveAllCookiesConstruct extends PhantomJSConstruct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("page", ATOMIC));
    }

    /**
     * Tries to delete all cookies from a page in a {@link MetaExpression}.
     *
     * @param page       The {@link MetaExpression} containing the page.
     * @return Returns NULL.
     */
    private MetaExpression process(final MetaExpression page) {

        if (page.isNull()) {
            return NULL;
        }

        checkPageType(page);

        WebVariable driver = getPage(page);
        getWebService().deleteCookies(driver);
        return NULL;
    }

}
