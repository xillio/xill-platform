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
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.services.json.JsonException;
import nl.xillio.xill.services.json.JsonParser;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Forwards a JSON string to GSON
 */
public class ParseJSONConstruct extends Construct {

    @Inject
    private JsonParser jsonParser;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                json -> process(json, jsonParser),
                new Argument("json", ATOMIC, LIST));
    }

    static MetaExpression process(final MetaExpression json, final JsonParser jsonParser) {
        assertNotNull(json, "input");

        if (json.getType() == ExpressionDataType.LIST) {
            ArrayList<MetaExpression> input = json.getValue();

            if(input.isEmpty()){
                throw new InvalidUserInputException("The provided LIST is empty", json.getStringValue(),
                        "A LIST containing ATOMICS that represent JSON strings", "[\"{\\\"1\\\":\\\"value\\\",\\\"2\\\":\\\"anotherValue\\\"}\"]");
            }

            ArrayList<MetaExpression> output = input.stream()
                    .map(value -> processAtomic(value, jsonParser))
                    .collect(Collectors.toCollection(ArrayList::new));

            return fromValue(output);
        } else {
            return processAtomic(json, jsonParser);
        }
    }

    private static MetaExpression processAtomic(final MetaExpression atomicJson, final JsonParser jsonParser) {
        assertNotNull(atomicJson, "input");

        if (atomicJson.getType() != ExpressionDataType.ATOMIC) {
            throw new InvalidUserInputException("Could not parse JSON from something else than an ATOMIC.",
                    atomicJson.getStringValue(), "An ATOMIC representing a JSON string", "\"{\\\"1\\\":\\\"value\\\",\\\"2\\\":\\\"anotherValue\\\"}\"");
        }

        String jsonValue = atomicJson.getStringValue();

        try {
            Object result = jsonParser.fromJson(jsonValue, Object.class);
            return parseObject(result);
        } catch (JsonException e) {
            Throwable exception = ExceptionUtils.getRootCause(e);
            if (exception == null) {
                exception = e;
            }

            throw new OperationFailedException("parse JSON input", exception.getMessage(), e);
        }
    }
}
