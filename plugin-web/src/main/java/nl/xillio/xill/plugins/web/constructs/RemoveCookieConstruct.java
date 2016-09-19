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
import nl.xillio.xill.plugins.web.PhantomJSConstruct;
import nl.xillio.xill.plugins.web.data.WebVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;

import java.util.List;

/**
 * Removes cookie from a currently loaded page context
 */
public class RemoveCookieConstruct extends PhantomJSConstruct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (page, cookie) -> process(page, cookie, getWebService()),
                new Argument("page", ATOMIC),
                new Argument("cookie", ATOMIC, LIST));
    }

    /**
     * @param pageVar    input variable (should be of a PAGE type)
     * @param cookieVar  input variable - string (cookie name) or list of strings or boolean
     * @param webService the webService we're using.
     * @throws OperationFailedException if an (unknown) error has occurred
     * @return null variable
     */
    public static MetaExpression process(final MetaExpression pageVar, final MetaExpression cookieVar, final WebService webService) {

        if (cookieVar.isNull() || pageVar.isNull()) {
            return NULL;
        }

        checkPageType(pageVar);
        WebVariable driver = getPage(pageVar);
        String cookieName = "";
        try {
            if (cookieVar.getType() == LIST) {
                @SuppressWarnings("unchecked")
                List<MetaExpression> list = (List<MetaExpression>) cookieVar.getValue();
                for (MetaExpression cookie : list) {
                    cookieName = cookie.getStringValue();
                    webService.deleteCookieNamed(driver, cookieName);
                }
            } else {
                cookieName = cookieVar.getStringValue();
                webService.deleteCookieNamed(driver, cookieName);
            }
        } catch (Exception e) {
            throw new OperationFailedException("delete cookie: " + cookieName, "Unknown error: " + e.getMessage(), e);
        }
        return NULL;
    }
}
