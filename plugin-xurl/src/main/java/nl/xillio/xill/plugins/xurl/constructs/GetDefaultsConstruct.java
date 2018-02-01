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
package nl.xillio.xill.plugins.xurl.constructs;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.xurl.data.Options;
import nl.xillio.xill.plugins.xurl.services.OptionsFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Returns the default options set on the plugin.
 *
 * @author Andrea Parrilli
 */
public class GetDefaultsConstruct extends Construct {
    private final OptionsFactory optionsFactory;

    @Inject
    public GetDefaultsConstruct(OptionsFactory optionsFactory) {
        this.optionsFactory = optionsFactory;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                () -> process(context)
        );
    }

    private MetaExpression process(ConstructContext context) {
        Options options = optionsFactory.getDefaultOptions();

        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();

        // Make a Xill object for the given Options instance
        if(options.getBasicAuth() != null) {
            LinkedHashMap<String, MetaExpression> basicAuth = new LinkedHashMap<>();
            basicAuth.put("username", fromValue(options.getBasicAuth().getUsername()));
            basicAuth.put("password", fromValue(options.getBasicAuth().getPassword()));
            result.put(OptionsFactory.Option.BASIC_AUTH.label(), fromValue(basicAuth));
        }

        result.put(OptionsFactory.Option.ENABLE_REDIRECT.label(), fromValue(options.isEnableRedirect()));

        if(options.getHeaders().length > 0) {
            Map<String, MetaExpression> headers =
                    Arrays.stream(options.getHeaders())
                            .collect(Collectors.toMap(h -> getName(), h -> fromValue(h.getValue())));
            result.put(OptionsFactory.Option.HEADERS.label(), fromValue(new LinkedHashMap<>(headers)));
        }

        result.put(OptionsFactory.Option.IGNORE_CONNECTION_CACHE.label(), fromValue(options.isIgnoreConnectionCache()));

        result.put(OptionsFactory.Option.INSECURE.label(), fromValue(options.isInsecure()));

        if(options.getLogging() != null) {
            result.put(OptionsFactory.Option.LOGGING.label(), fromValue(options.getLogging()));
        }

        result.put(OptionsFactory.Option.MULTIPART.label(), fromValue(options.isMultipart()));

        if(options.getNTLMOptions() != null) {
            LinkedHashMap<String, MetaExpression> ntlm = new LinkedHashMap<>();
            ntlm.put("username", fromValue(options.getNTLMOptions().getUsername()));
            ntlm.put("password", fromValue(options.getNTLMOptions().getPassword()));
            ntlm.put("workstation", fromValue(options.getNTLMOptions().getWorkstation()));
            result.put(OptionsFactory.Option.NTLM.label(), fromValue(ntlm));
        }

        if(options.getResponseContentType() != null) {
            result.put(OptionsFactory.Option.RESPONSE_CONTENT_TYPE.label(), fromValue(options.getResponseContentType().getMimeType()));
        }

        result.put(OptionsFactory.Option.TIMEOUT.label(), fromValue(options.getTimeout()));

        return fromValue(result);
    }
}
