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
package nl.xillio.xill.plugins.system.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.services.json.JsonException;
import nl.xillio.xill.services.json.JsonParser;
import nl.xillio.xill.services.json.PrettyJsonParser;

/**
 * Returns a json string representation of the input.
 *
 * @author Thomas Biesaart
 */
public class ToJSONConstruct extends Construct {

    @Inject
    private JsonParser jsonParser;
    @Inject
    private PrettyJsonParser prettyJsonParser;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor((expression, pretty) -> process(expression, pretty, jsonParser, prettyJsonParser), new Argument("expression"), new Argument("pretty", FALSE, ATOMIC));
    }

    static MetaExpression process(final MetaExpression expression, final MetaExpression pretty, final JsonParser parser, final JsonParser prettyParser) {
        JsonParser jsonParser = pretty.getBooleanValue() ? prettyParser : parser;

        try {
            return fromValue(jsonParser.toJson(expression));
        } catch (JsonException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        }
    }
}
