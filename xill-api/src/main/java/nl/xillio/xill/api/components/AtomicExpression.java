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

import nl.xillio.xill.api.io.IOStream;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This {@link MetaExpression} represents an expression that holds an {@link ExpressionDataType#ATOMIC} value.
 */
class AtomicExpression extends MetaExpression {

    private final Expression expressionValue;

    /**
     * Creates a new atomic expression that hosts an {@link Expression}.
     *
     * @param value the value to set
     */
    public AtomicExpression(final Expression value) {
        setValue(value);

        // Save to prevent casting
        expressionValue = value;
    }

    /**
     * Creates a new atomic expression with a {@link BooleanBehavior}.
     *
     * @param value the value to set
     */
    public AtomicExpression(final boolean value) {
        this(new BooleanBehavior(value));
    }

    /**
     * Creates a new atomic expression with a {@link NumberBehavior}.
     *
     * @param value the value to set
     */
    public AtomicExpression(final Number value) {
        this(new NumberBehavior(value));
    }

    /**
     * Creates a new atomic with a {@link StringBehavior}.
     *
     * @param value the value to set
     */
    public AtomicExpression(final String value) {
        this(new StringBehavior(value));
    }

    @Override
    public Number getNumberValue() {
        return expressionValue.getNumberValue();
    }

    @Override
    public String getStringValue() {
        return expressionValue.getStringValue();
    }

    @Override
    public boolean getBooleanValue() {
        return expressionValue.getBooleanValue();
    }

    @Override
    public boolean isNull() {
        return expressionValue.isNull();
    }

    @Override
    public IOStream getBinaryValue() {
        return expressionValue.getBinaryValue();
    }

    @Override
    public void close() {
        super.close();
        expressionValue.close();
    }

    @Override
    public Collection<Processable> getChildren() {
        return new ArrayList<>();
    }

}
