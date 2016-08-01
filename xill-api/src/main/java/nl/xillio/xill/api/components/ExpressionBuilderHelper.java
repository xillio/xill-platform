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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * This class contains various useful utility functions to create Expressions.
 */
public class ExpressionBuilderHelper {

    /**
     * @see ExpressionDataType#LIST
     */
    protected static final ExpressionDataType LIST = ExpressionDataType.LIST;
    /**
     * @see ExpressionDataType#ATOMIC
     */
    protected static final ExpressionDataType ATOMIC = ExpressionDataType.ATOMIC;
    /**
     * @see ExpressionDataType#OBJECT
     */
    protected static final ExpressionDataType OBJECT = ExpressionDataType.OBJECT;

    /**
     * The true literal
     */
    public static final MetaExpression TRUE = new ImmutableLiteral(new BooleanBehavior(true));
    /**
     * The false literal
     */
    public static final MetaExpression FALSE = new ImmutableLiteral(new BooleanBehavior(false));

    /**
     * The null literal
     */
    public static final MetaExpression NULL = new ImmutableLiteral(NullLiteral.Instance);

    /**
     * Creates a new expression containing binary data.
     *
     * @param value the value of the expression
     * @return the expression
     */
    public static MetaExpression fromValue(final IOStream value) {
        return buildAtomicOrNull(value, BinaryBehavior::new);
    }

    /**
     * Creates a new expression containing a number.
     *
     * @param value the value of the expression
     * @return the expression
     */
    public static MetaExpression fromValue(final Number value) {
        return buildAtomicOrNull(value, NumberBehavior::new);
    }

    /**
     * Creates a new expression containing a boolean.
     *
     * @param value the value of the expression
     * @return the expression
     */
    public static MetaExpression fromValue(final boolean value) {
        return value ? TRUE : FALSE;
    }

    /**
     * Creates a new expression containing a string.
     *
     * @param value the value of the expression
     * @return the expression
     */
    public static MetaExpression fromValue(final String value) {
        return fromValue(value, false);
    }

    /**
     * Creates a new constant expression.
     *
     * @param value      the value
     * @param isConstant true if this expression should be a string constant
     * @return the expression
     */
    public static MetaExpression fromValue(final String value, final boolean isConstant) {
        if (!isConstant) {
            return buildAtomicOrNull(value, StringBehavior::new);
        }
        return buildAtomicOrNull(value, StringConstantBehavior::new);
    }

    /**
     * Creates a new {@link ExpressionDataType#LIST} containing a value.
     * For empty lists you can use {@link ExpressionBuilderHelper#emptyList()}.
     *
     * @param value the value of the expression
     * @return the expression
     */
    public static MetaExpression fromValue(final List<MetaExpression> value) {
        return buildOrNull(value, ListExpression::new);
    }

    /**
     * Creates a new {@link ExpressionDataType#OBJECT} containing a value.
     * For empty lists you can use {@link ExpressionBuilderHelper#emptyObject()}.
     *
     * @param value the value of the expression
     * @return the expression
     */
    @SuppressWarnings("squid:S1319")
    // We should use LinkedHashMap as a parameter here to enforce ordering in the map
    public static MetaExpression fromValue(final LinkedHashMap<String, MetaExpression> value) {
        return buildOrNull(value, ObjectExpression::new);
    }

    /**
     * Creates a new {@link ListExpression} with no values (empty List).
     *
     * @return the expression
     */
    public static MetaExpression emptyList() {
        return fromValue(new ArrayList<>());
    }

    /**
     * Creates a new {@link ObjectExpression} with no values.
     *
     * @return the expression
     */
    public static MetaExpression emptyObject() {
        return fromValue(new LinkedHashMap<>());
    }

    /**
     * A shortcut to {@link MetaExpression#extractValue(MetaExpression)}.
     *
     * @param expression The expression to extract Java objects from
     * @return The value specified in
     * {@link MetaExpression#extractValue(MetaExpression)}
     * @see MetaExpression#extractValue(MetaExpression)
     */
    protected static Object extractValue(final MetaExpression expression) {
        return MetaExpression.extractValue(expression);
    }

    /**
     * A shortcut to {@link MetaExpression#parseObject(Object)}.
     *
     * @param value the object to parse into a {@link MetaExpression}
     * @return the {@link MetaExpression} not null
     * @throws IllegalArgumentException if parsing the value failed
     * @see MetaExpression#parseObject(Object)
     */
    protected static MetaExpression parseObject(final Object value) throws IllegalArgumentException {
        return MetaExpression.parseObject(value);
    }

    private static <T> MetaExpression buildAtomicOrNull(T value, Function<T, AbstractBehavior> builder) {
        return buildOrNull(value, input -> new AtomicExpression(builder.apply(input)));
    }

    private static <T> MetaExpression buildOrNull(T value, Function<T, MetaExpression> builder) {
        if (value == null) {
            return NULL;
        }

        return builder.apply(value);
    }

    /**
     * This {@link Expression} represents a null in literal form.
     */
    private static final class NullLiteral extends AbstractBehavior {

        /**
         * The single instance of the null literal.
         */
        static final NullLiteral Instance = new NullLiteral();

        private NullLiteral() {
        }

        @Override
        public Number getNumberValue() {
            return Double.NaN;
        }

        @Override
        public String getStringValue() {
            return "null";
        }

        @Override
        public boolean getBooleanValue() {
            return false;
        }

        @Override
        public boolean isNull() {
            return true;
        }

        @Override
        public Expression copy() {
            return new NullLiteral();
        }

    }
}
