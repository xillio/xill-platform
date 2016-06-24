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
package nl.xillio.xill.plugins.rest.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.data.XmlNodeFactory;
import nl.xillio.xill.plugins.rest.data.Content;
import nl.xillio.xill.plugins.rest.data.Options;
import nl.xillio.xill.plugins.rest.services.RestService;
import nl.xillio.xill.services.json.JsonParser;

/**
 * Returns content of HEAD Rest command.
 *
 * @author Zbynek Hochmann
 */
@Deprecated
public class HeadConstruct extends AbstractRequestConstruct {

    @Inject
    protected HeadConstruct(RestService restService, JsonParser jsonParser, XmlNodeFactory xmlNodeFactory) {
        super(restService, jsonParser, xmlNodeFactory, false);
    }

    @Override
    protected Content process(String url, Options options, Content body) {
        return restService().head(url, options);
    }
}
