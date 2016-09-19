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
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.web.PhantomJSConstruct;
import nl.xillio.xill.plugins.web.data.CookieFactory;
import nl.xillio.xill.plugins.web.data.WebVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;

import java.util.List;
import java.util.Map;

/**
 * Set cookie in a currently loaded page context
 */
public class SetCookieConstruct extends PhantomJSConstruct {
    @Inject
    private CookieFactory cookieFactory;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (page, cookies) -> process(page, cookies, cookieFactory, getWebService()),
                new Argument("page"),
                new Argument("cookies", LIST, OBJECT));
    }

    /**
     * @param pageVar       input variable (should be of a PAGE type)
     * @param cookiesVar    input string variable - associated array or list of associated arrays (see CT help for details)
     * @param cookieFactory The factory which builds cookies.
     * @param webService    The service we're using for accesing the web.
     * @throws OperationFailedException if the meta expression did not contain a cookie
     * @return null variable
     */
    public static MetaExpression process(final MetaExpression pageVar, final MetaExpression cookiesVar, final CookieFactory cookieFactory, final WebService webService) {

        if (cookiesVar.isNull() || pageVar.isNull()) {
            return NULL;
        }

        checkPageType(pageVar);
        WebVariable driver = getPage(pageVar);

        if (cookiesVar.getType() == LIST) {
            @SuppressWarnings("unchecked")
            List<MetaExpression> list = (List<MetaExpression>) cookiesVar.getValue();
            for (MetaExpression cookie : list) {
                processCookie(driver, cookie, cookieFactory, webService);
            }
        } else {
            processCookie(driver, cookiesVar, cookieFactory, webService);
        }

        return NULL;
    }

    private static void processCookie(final WebVariable driver, final MetaExpression cookie, final CookieFactory cookieFactory, final WebService webService) {
        if (cookie.getType() != OBJECT) {
            throw new OperationFailedException("Failed to process a cookie", "No OBJECT could be found when processing: " + cookie.getStringValue());
        }
        Map<String, MetaExpression> cookieMap = (Map<String, MetaExpression>) cookie.getValue();
        cookieFactory.setCookie(driver, cookieMap, webService);
    }
}
