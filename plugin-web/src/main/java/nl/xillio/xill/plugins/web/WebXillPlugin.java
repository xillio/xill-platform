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
package nl.xillio.xill.plugins.web;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import nl.xillio.plugins.XillPlugin;
import nl.xillio.xill.api.XillThreadFactory;
import nl.xillio.xill.plugins.web.data.Options;
import nl.xillio.xill.plugins.web.data.PhantomJSPool;

/**
 * This package includes all Selenium constructs
 */
public class WebXillPlugin extends XillPlugin {
    private PhantomJSPool phantomJSPool;

    public WebXillPlugin() {
        Options.extractNativeBinary();
    }

    @Override
    public void close() {
        phantomJSPool.close();
        super.close();
    }

    @Singleton
    @Provides
    public PhantomJSPool providesPhantomJSPool(XillThreadFactory xillThreadFactory) {
        phantomJSPool = new PhantomJSPool(xillThreadFactory);
        return phantomJSPool;
    }
}
