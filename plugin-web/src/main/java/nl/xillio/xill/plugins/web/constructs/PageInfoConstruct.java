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
import org.openqa.selenium.Cookie;

import java.util.LinkedHashMap;

/**
 * Returns the info about currently loaded web page
 */
public class PageInfoConstruct extends PhantomJSConstruct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                page -> process(page, webService),
                new Argument("page", ATOMIC));
    }

    /**
     * @param pageVar    input variable (should be of a PAGE type)
     * @param webService the webService we're using.
     * @return list of string variable
     */
    public static MetaExpression process(final MetaExpression pageVar, final WebService webService) {

        if (pageVar.isNull()) {
            return NULL;
        }

        checkPageType(pageVar);
        WebVariable driver = getPage(pageVar);
        LinkedHashMap<String, MetaExpression> list = new LinkedHashMap<>();

        list.put("url", fromValue(webService.getCurrentUrl(driver)));
        list.put("title", fromValue(webService.getTitle(driver)));

        LinkedHashMap<String, MetaExpression> cookies = new LinkedHashMap<>();
        for (Cookie cookie : webService.getCookies(driver)) {
            cookies.put(webService.getName(cookie), makeCookie(cookie, webService));
        }
        list.put("cookies", fromValue(cookies));
        return fromValue(list);
    }
}
