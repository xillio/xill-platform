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
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.plugins.web.InvalidUrlException;
import nl.xillio.xill.plugins.web.data.Options;
import nl.xillio.xill.plugins.web.data.PhantomJSPool;
import nl.xillio.xill.plugins.web.data.WebVariable;

import java.net.MalformedURLException;

/**
 * @author Zbynek Hochmann
 *         Loads the new page via PhantomJS process and holds the context of a page
 */
public class LoadPageConstruct extends PhantomJSConstruct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (url, options) -> process(url, options),
                new Argument("url", ATOMIC),
                new Argument("options", NULL, OBJECT));
    }

    /**
     * @param urlVar         string variable - page URL
     * @param optionsVar     list variable - options for loading the page (see CT help for details)
     * @throws InvalidUserInputException if the URL is invalid
     * @return PAGE variable
     */
    MetaExpression process(final MetaExpression urlVar, final MetaExpression optionsVar) {
        String url = urlVar.getStringValue();
        Options options;

        // processing input options
        options = getOptionsFactory().processOptions(optionsVar);
        if (options == null) {
            options = new Options();
        }

        try (PhantomJSPool.Entity entity = getWebService().getEntityFromPool(getPhantomJSPool(), options)) {
            WebVariable page = entity.getPage();
            getWebService().httpGet(page, url);
            return createPage(page, getWebService());
        } catch (MalformedURLException | InvalidUrlException e) {
            throw new InvalidUserInputException("Malformed or Invalid URL during httpGet.", url, "a valid (non relative) URL", e);
        }
    }
}
