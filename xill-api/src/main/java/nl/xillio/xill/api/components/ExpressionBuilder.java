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
package nl.xillio.xill.api.components;

import nl.xillio.xill.api.Debugger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * This is a utility class that will build expressions from values when processed.
 * This is generally used to create representations of the literal values at runtime.
 */
public class ExpressionBuilder extends ExpressionBuilderHelper implements Processable {

    private final Function<Debugger, MetaExpression> expressionFunction;

    /**
     * Creates a new expression builder that will produce a Number.
     *
     * @param value the value to set
     */
    public ExpressionBuilder(final Number value) {
        expressionFunction = debugger -> fromValue(value);
    }

    /**
     * Creates a new expression builder that will produce a string.
     *
     * @param value the value to set
     */
    public ExpressionBuilder(final String value) {
        expressionFunction = debugger -> fromValue(value);
    }

    /**
     * Creates a new expression builder that will produce a string.
     *
     * @param value      the value to set
     * @param isConstant whether to create a string constant
     */
    public ExpressionBuilder(final String value, final boolean isConstant) {
        expressionFunction = debugger -> fromValue(value, isConstant);
    }

    /**
     * Creates a new expression builder that will produce a List.
     *
     * @param value    the value to set
     */
    public ExpressionBuilder(final List<Processable> value) {
        expressionFunction = debugger -> {
            List<MetaExpression> result = new ArrayList<>();

            for (Processable proc : value) {
                result.add(proc.process(debugger).get());
            }

            return fromValue(result);
        };
    }

    /**
     * Creates a new expression builder that will produce an object.
     *
     * @param value    the value to set
     */
    @SuppressWarnings("squid:S1319")
    // We should use LinkedHashMap as a parameter here to enforce ordering in the map
    public ExpressionBuilder(final LinkedHashMap<Processable, Processable> value) {
        expressionFunction = debugger -> {
            LinkedHashMap<String, MetaExpression> entries = new LinkedHashMap<>();

            value.forEach((key, expression) -> entries.put(key.process(debugger).get().getStringValue(), expression.process(debugger).get()));

            return fromValue(entries);
        };
    }

    /**
     * Creates a new expression builder that will produce a double.
     *
     * @param value the value to set
     */
    public ExpressionBuilder(final boolean value) {
        expressionFunction = debugger -> fromValue(value);
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) {
        return InstructionFlow.doResume(expressionFunction.apply(debugger));
    }

    @Override
    public Collection<Processable> getChildren() {
        return new ArrayList<>();
    }
}
