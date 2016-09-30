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
package nl.xillio.xill.services.inject;

import com.google.inject.AbstractModule;
import me.biesaart.utils.Log;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.XillThreadFactory;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.services.files.TextFileReader;
import nl.xillio.xill.services.json.JacksonParser;
import nl.xillio.xill.services.json.JsonParser;
import nl.xillio.xill.services.json.PrettyJsonParser;
import org.slf4j.Logger;

/**
 * This is the main module that will run for the injector at runtime.
 *
 * @author Thomas Biesaart
 */
public class DefaultInjectorModule extends AbstractModule {

    private static final Logger LOGGER = Log.get();
    private final XillEnvironment xillEnvironment;
    private final XillThreadFactory xillThreadFactory;

    public DefaultInjectorModule(XillEnvironment xillEnvironment, XillThreadFactory xillThreadFactory) {
        this.xillEnvironment = xillEnvironment;
        this.xillThreadFactory = xillThreadFactory;
    }

    @Override
    protected void configure() {
        try {
            //Some default injectors
            bind(String[].class).toInstance(new String[0]);
            bind(int[].class).toInstance(new int[0]);
            bind(boolean[].class).toInstance(new boolean[0]);

            //Some generic dependencies for plugins
            bind(ProcessBuilder.class).toConstructor(ProcessBuilder.class.getConstructor(String[].class));
            bind(JsonParser.class).toInstance(new JacksonParser(false));
            bind(PrettyJsonParser.class).toInstance(new JacksonParser(true));
            bind(XillEnvironment.class).toInstance(xillEnvironment);
            bind(XillThreadFactory.class).toInstance(xillThreadFactory);

            requestStaticInjection(Construct.class);
            requestStaticInjection(MetaExpression.class);

        } catch (NoSuchMethodException | SecurityException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
