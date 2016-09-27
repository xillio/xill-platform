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
import nl.xillio.xill.plugins.web.InvalidUrlException;
import nl.xillio.xill.plugins.web.PhantomJSConstruct;
import nl.xillio.xill.plugins.web.data.Options;
import nl.xillio.xill.plugins.web.data.OptionsFactory;
import nl.xillio.xill.plugins.web.data.PhantomJSPool;
import nl.xillio.xill.plugins.web.data.WebVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;

import java.net.MalformedURLException;

/**
 * @author Zbynek Hochmann
 *         Loads the new page via PhantomJS process and holds the context of a page
 */
public class LoadPageConstruct extends PhantomJSConstruct implements AutoCloseable {
    @Inject
    private OptionsFactory optionsFactory;

    @Inject
    private PhantomJSPool pool;

    static {
        Options.extractNativeBinary();
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (url, options) -> process(url, options, optionsFactory, getWebService(), pool),
                new Argument("url", ATOMIC),
                new Argument("options", NULL, OBJECT));
    }

    /**
     * @param urlVar         string variable - page URL
     * @param optionsVar     list variable - options for loading the page (see CT help for details)
     * @param optionsFactory The factory which will create the options.
     * @param webService     The service we're using.
     * @throws OperationFailedException if the URL is invalid
     * @return PAGE variable
     */
    public static MetaExpression process(final MetaExpression urlVar, final MetaExpression optionsVar, final OptionsFactory optionsFactory, final WebService webService, PhantomJSPool pool) {
        String url = urlVar.getStringValue();
        Options options;
        PhantomJSPool.Entity entity = null;

        try {
            // processing input options
            options = optionsFactory.processOptions(optionsVar);
            if (options == null) {
                options = new Options();
            }
            entity = webService.getEntityFromPool(pool, options);
            WebVariable page = entity.getPage();
            webService.httpGet(page, url);
            return createPage(page, webService);
        } catch (MalformedURLException | InvalidUrlException e) {
            release(entity);
            throw new OperationFailedException("load the page.", "Malformed or Invalid URL during httpGet.", e);
        }
    }

    private static void release(PhantomJSPool.Entity entity) {
        if (entity != null) {
            entity.release();
        }
    }

    @Override
    public void close() throws Exception {
        // it will dispose entire PJS pool (all PJS processes will be terminated and temporary PJS files deleted)
        pool.dispose();
    }
}
