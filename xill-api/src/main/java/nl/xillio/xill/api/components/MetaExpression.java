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

import com.google.inject.Inject;
import com.google.inject.Injector;
import nl.xillio.util.MathUtils;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.api.data.DateFactory;
import nl.xillio.xill.api.data.MetadataExpression;
import nl.xillio.xill.api.errors.NotImplementedException;
import nl.xillio.xill.api.errors.RobotConcurrentModificationException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.services.json.JsonException;
import nl.xillio.xill.services.json.JsonParser;

import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * This class represents a general expression in the Xill language.
 */
public abstract class MetaExpression implements Expression, Processable {

    /**
     * Enable this to get debug information.
     * This will prevent disposal of the value field and add it to the error messages.
     */
    private static final boolean DEBUG = false;
    private Throwable closedLocation;

    @Inject
    private static JsonParser jsonParser;
    @Inject
    private static Injector injector;

    private MetadataExpressionPool<MetadataExpression> metadataPool;
    private Object value;
    private ExpressionDataType type = ExpressionDataType.ATOMIC;
    private boolean isClosed;
    private int referenceCount;
    private boolean preventDispose;

    /**
     * Gets a value from the {@link MetadataExpressionPool}.
     *
     * @param <T>   the type of object to fetch
     * @param clazz the type of class to fetch. <b>Note</b> that this doesn't take inheritance into account.
     * @return the stored value or null if none was found
     * @throws IllegalStateException if this expression has been closed
     */
    public <T extends MetadataExpression> T getMeta(final Class<T> clazz) {

        if (metadataPool == null) {
            return null;
        }

        return metadataPool.get(clazz);
    }

    protected void assertOpen() {
        if (isClosed()) {
            String message = "This expression has already been closed.";
            if (DEBUG) {
                throw new IllegalStateException(message, closedLocation);
            }
            throw new IllegalStateException(message);
        }
    }

    protected boolean isOpen(){
        return !isClosed;
    }

    /**
     * Stores a value in the {@link MetadataExpressionPool} and default the key to the class of the object as returned by {@link Object#getClass()}.
     *
     * @param object the object to store
     * @throws IllegalStateException if this expression has been closed
     */
    @SuppressWarnings("unchecked")
    public void storeMeta(final MetadataExpression object) {
        assertOpen();
        if (metadataPool == null) {
            metadataPool = new MetadataExpressionPool<>();
        }

        metadataPool.put(object);
    }

    /**
     * Checks if this expression has a class in its metadata.
     *
     * @param clazz the class to check
     * @return true if and only if a class has been found that implements or extends the class to check
     */
    public boolean hasMeta(Class<? extends MetadataExpression> clazz) {
        return metadataPool != null && metadataPool.hasValue(clazz);
    }

    /**
     * Sets the value of this variable to an {@link ExpressionDataType#ATOMIC} value.
     *
     * @param value the {@link ExpressionDataType#ATOMIC} value to store
     * @return self
     * @throws IllegalStateException if this expression has been closed
     */
    protected MetaExpression setValue(final Expression value) {
        assertOpen();
        this.value = value;
        type = ExpressionDataType.ATOMIC;
        return this;
    }

    /**
     * Sets the value of this expression to that of another one.
     *
     * @param value the other {@link MetaExpression} to copy the value from
     * @return this
     * @throws IllegalStateException if this expression has been closed
     */
    protected MetaExpression setValue(final MetaExpression value) {
        assertOpen();
        this.value = value.getValue();
        type = value.getType();
        return this;
    }

    /**
     * Sets the value of this variable to a {@link ExpressionDataType#LIST} value.
     *
     * @param value the {@link ExpressionDataType#LIST} to set the value to
     * @return self
     * @throws IllegalStateException if this expression has been closed
     */
    protected MetaExpression setValue(final List<MetaExpression> value) {
        assertOpen();
        this.value = value;
        type = ExpressionDataType.LIST;
        return this;
    }

    /**
     * Sets the value of this variable to an {@link ExpressionDataType#OBJECT} value.
     *
     * @param value in a linked hash map to enforce order
     * @return self
     * @throws IllegalStateException if this expression has been closed
     */
    protected MetaExpression setValue(final LinkedHashMap<String, MetaExpression> value) {
        assertOpen();
        this.value = value;
        type = ExpressionDataType.OBJECT;
        return this;
    }

    /**
     * <p>
     * Gets the contained value of this {@link MetaExpression}.
     * The return type of this method will be different for each type of {@link ExpressionDataType},
     * which can be found by calling {@link #getType()}:
     * </p>
     * <ul>
     * <li>{@link ExpressionDataType#ATOMIC}: Returns an {@link Expression}</li>
     * <li>{@link ExpressionDataType#LIST}: Returns a {@link List
     * List&lt;MetaExpression&gt;}</li>
     * <li>{@link ExpressionDataType#OBJECT}: Returns a {@link Map
     * Map&lt;String, MetaExpression&gt;}</li>
     * </ul>
     *
     * @param <T> the return type
     * @return the value according to the {@link ExpressionDataType} specification
     * @throws IllegalStateException if this expression has been closed
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        assertOpen();
        return (T) value;
    }

    /**
     * <p>
     * Returns the type of data stored in this {@link MetaExpression}:
     * </p>
     * <ul>
     * <li>{@link ExpressionDataType#ATOMIC}: A single value</li>
     * <li>{@link ExpressionDataType#LIST}: A list of {@link MetaExpression}</li>
     * <li>{@link ExpressionDataType#OBJECT}: An object with {@link String} indices</li>
     * </ul>
     *
     * @return the type
     * @throws IllegalStateException if this expression has been closed
     */
    public ExpressionDataType getType() {
        assertOpen();
        return type;
    }

    /**
     * <p>
     * Generates the JSON representation of this expression using a {@link JsonParser} parser.
     * </p>
     * <b>NOTE: </b> This is not the string value of this expression. It is
     * JSON. For the string value use {@link MetaExpression#getStringValue()}.
     *
     * @return JSON representation
     */
    @Override
    public String toString() {
        try {
            return toString(jsonParser);
        } catch (JsonException e) {
            throw new RobotRuntimeException("Failed to parse expression to string.", e);
        }
    }

    /**
     * <p>
     * Generates the JSON representation of this expression using a {@link JsonParser}.
     * </p>
     * <p>
     * <b>NOTE: </b> This is not the string value of this expression. It is JSON. For the string value use {@link MetaExpression#getStringValue()}.
     * </p>
     *
     * @param jsonParser the json parser that should be used
     * @return JSON representation
     * @throws JsonException if the value cannot be parsed by the JsonParser
     */
    public String toString(final JsonParser jsonParser) throws JsonException {
        return jsonParser.toJson((Object) extractValue(this));
    }

    public Number getSize() {
        return null;
    }

    @Override
    public boolean equals(final Object obj) {
        // First check if these objects are the same instance
        if (obj == this) {
            return true;
        }

        if (obj instanceof MetaExpression) {
            return valueEquals((MetaExpression) obj);
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        if (getType().equals(ExpressionDataType.ATOMIC)) {
            return getStringValue().hashCode();
        }
        return getValue().hashCode();
    }

    /**
     * Checks equality on meta expression on the value level. This uses the equals method.
     *
     * @param other the other metaclose expression
     * @return true if and only if the expressions have equal value
     */
    public boolean valueEquals(final MetaExpression other) {
        // Compare the types.
        if (getType() != other.getType()) {
            return false;
        }

        // Compare the values.
        switch (getType()) {
            case ATOMIC:
                return compareAtomic(other);
            case LIST:
            case OBJECT:
                return getValue().equals(other.getValue());
            default:
                throw new NotImplementedException("This type has not been implemented.");
        }
    }

    private boolean compareAtomic(MetaExpression other) {
        // Check if one xor the other is null.
        if (isNull() != other.isNull()) {
            return false;
        }

        // Compare the string values.
        if (getStringValue().equals(other.getStringValue())) {
            return true;
        }

        // If both atomics might be numbers, only compare their number values.

        // If one of them is not a number don't compare number value
        Number number = getNumberValue();
        if (Double.isNaN(number.doubleValue())) {
            return false;
        }
        Number otherNumber = other.getNumberValue();
        if (Double.isNaN(otherNumber.doubleValue())) {
            return false;
        }

        return MathUtils.compare(number, otherNumber) == 0;
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) {
        return InstructionFlow.doResume(this);
    }

    /**
     * Extracts the actual Java Object value for this expression.
     *
     * @param expression the {@link MetaExpression} to extract a value from
     * @param <T>        the return type
     * @return <ul>
     * <li>{@link ExpressionDataType#ATOMIC}: returns an {@link Object} This represents a singular value that is parsed in the folowing way:
     * <ol>
     * <li>If the value is null: return null</li>
     * <li>If the expression is created using {@link ExpressionBuilder#fromValue(boolean)}: return {@link Boolean}</li>
     * <li>If the value can be a number (so also string constants like "5.7") it will return a {@link Double}</li>
     * <li>In all other cases return a {@link String} representation.
     * </ol>
     * </li>
     * <li>{@link ExpressionDataType#LIST}: returns a {@link List
     * List&lt;Object&gt;} where {@link Object} is the result of a recursive call of this method.</li>
     * <li>{@link ExpressionDataType#OBJECT}: returns a {@link Map
     * Map&lt;String, Object&gt;} where {@link Object} is the result of a recursive call of this method.</li>
     * </ul>
     */
    public static <T> T extractValue(final MetaExpression expression) {
        return extractValue(expression, MetaExpressionSerializer.NULL);
    }

    @SuppressWarnings("unchecked")
    public static <T> T extractValue(final MetaExpression expression, final MetaExpressionSerializer metaExpressionSerializer) {
        return (T) extractValue(expression, new IdentityHashMap<>(), metaExpressionSerializer);
    }

    private static Object extractValue(final MetaExpression expression, final Map<MetaExpression, Object> results, final MetaExpressionSerializer metaExpressionSerializer) {

        if (results.containsKey(expression)) {
            return results.get(expression);
        }
        Object result;
        switch (expression.getType()) {
            case ATOMIC:
                // null
                if (expression.isNull()) {
                    return null;
                }

                result = metaExpressionSerializer.extractValue(expression);

                if (result != null) {
                    return result;
                }

                // First we check for the presence of a date
                Date date = expression.getMeta(Date.class);
                if (date != null) {
                    // We have a Date, convert it to a java.util.Date
                    return java.util.Date.from(date.getZoned().toInstant());
                }

                Object behaviour = expression.getValue();

                if (behaviour instanceof BooleanBehavior) {
                    result = expression.getBooleanValue();
                } else if (behaviour instanceof StringBehavior) {
                    result = expression.getStringValue();
                } else if (behaviour instanceof NumberBehavior) {
                    result = expression.getNumberValue();
                } else if (behaviour instanceof BinaryBehavior) {
                    result = expression.getStringValue();
                } else {
                    throw new UnsupportedOperationException("No extraction found for " + behaviour.getClass().getSimpleName());
                }
                break;
            case LIST:
                List<Object> resultList = new ArrayList<>();
                results.put(expression, resultList);
                try{
                    resultList.addAll((expression.<List<MetaExpression>>getValue()).stream().map(v -> extractValue(v, results, metaExpressionSerializer))
                            .collect(Collectors.toList()));
                } catch (ConcurrentModificationException cme){
                    throw new RobotConcurrentModificationException(cme);
                }
                result = resultList;
                break;
            case OBJECT:
                Map<String, Object> resultObject = new LinkedHashMap<>();
                results.put(expression, resultObject);
                for (Entry<String, MetaExpression> pair : (expression.<Map<String, MetaExpression>>getValue()).entrySet()) {
                    resultObject.put(pair.getKey(), extractValue(pair.getValue(), results, metaExpressionSerializer));
                }
                result = resultObject;
                break;
            default:
                throw new NotImplementedException("This type has not been implemented.");
        }
        return result;

    }

    /**
     * Registers a reference to this variable.
     * This generally only happens during assignment.
     */
    public final void registerReference() {
        // When this variable is assigned, take over ownership
        referenceCount++;
    }

    /**
     * Releases a reference to this expression.
     * This generally only happens at the end of scope.
     */
    public final void releaseReference() {
        referenceCount--;

        if (!preventDispose && referenceCount <= 0) {
            close();
        }
    }

    /**
     * Prevents this expression from being disposed.
     */
    public final void preventDisposal() {
        setPreventDispose(true);
    }

    /**
     * @return whether disposal is being prevented.
     */
    public final boolean isDisposalPrevented() {
        return preventDispose;
    }

    /**
     * Allows this expression to be disposed.
     */
    public final void allowDisposal() {
        setPreventDispose(false);
    }

    @SuppressWarnings("unchecked")
    private void setPreventDispose(boolean value) {
        if (preventDispose == value) {
            return;
        }

        preventDispose = value;

        if (getType() == ExpressionDataType.OBJECT) {
            ((Map<String, MetaExpression>) getValue())
                    .values()
                    .forEach(child -> child.setPreventDispose(value));
        } else if (getType() == ExpressionDataType.LIST) {
            ((List<MetaExpression>) getValue())
                    .forEach(child -> child.setPreventDispose(value));
        }

    }

    /**
     * @return whether this expression has been closed using {@link #close()}
     */
    private boolean isClosed() {
        return isClosed;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void close() {
        if (isClosed || this == ExpressionBuilderHelper.NULL) {
            return;
        }

        isClosed = true;
        closeMetaPool();

        // Close children
        switch (type) {
            case LIST:
                ((List<MetaExpression>) value).forEach(nl.xillio.xill.api.components.MetaExpression::releaseReference);
                break;
            case OBJECT:
                ((Map<String, MetaExpression>) value).values().forEach(nl.xillio.xill.api.components.MetaExpression::releaseReference);
                break;
            default:
                break;
        }
        if (!DEBUG) {
            value = null;
        } else {
            closedLocation = new RobotRuntimeException("This is where this expression got closed");
        }
    }


    /**
     * Disposes all items in the {@link MetadataExpressionPool}.
     */
    protected void closeMetaPool() {
        if (metadataPool != null) {
            metadataPool.close();
        }
    }

    /**
     * Resets the reference count to 0.
     */
    protected void resetReferences() {
        referenceCount = 0;
    }

    /**
     * Attempts to parse an object into a MetaExpression.
     *
     * @param value the object to parse
     * @return a {@link MetaExpression}, not null
     * @throws IllegalArgumentException when the value cannot be parsed
     */
    public static MetaExpression parseObject(final Object value) {
        return parseObject(value, MetaExpressionDeserializer.NULL);
    }

    /**
     * Attempts to parse an object into a MetaExpression.
     *
     * @param value                      the object to parse
     * @param metaExpressionDeserializer a specialized deserializer for plugin specific MetaExpressions
     * @return a {@link MetaExpression}, not null
     * @throws IllegalArgumentException when the value cannot be parsed
     */
    public static MetaExpression parseObject(final Object value, MetaExpressionDeserializer metaExpressionDeserializer) {
        return parseObject(value, new IdentityHashMap<>(), metaExpressionDeserializer);
    }

    @SuppressWarnings("unchecked")
    private static MetaExpression parseObject(final Object root, final Map<Object, MetaExpression> cache, final MetaExpressionDeserializer metaExpressionDeserializer) {
        // Check the cache (Don't use key because we need REFERENCE equality not CONTENT)
        for (Map.Entry<Object, MetaExpression> entry : cache.entrySet()) {
            if (entry.getKey() == root) {
                // It seems like we found our match
                return entry.getValue();
            }
        }

        if (root == null) {
            return ExpressionBuilder.NULL;
        }

        if (root instanceof List) {
            // Push stub
            MetaExpression result = ExpressionBuilderHelper.emptyList();
            cache.put(root, result);
            List<MetaExpression> values = result.getValue();

            // Parse children
            for (Object child : (List<?>) root) {
                MetaExpression current = parseObject(child, cache, metaExpressionDeserializer);
                values.add(current);
                current.registerReference();
            }

            // Push list
            result.setValue(values);

            return result;
        }

        if (root instanceof Map) {
            // Push stub
            MetaExpression result = ExpressionBuilderHelper.emptyObject();
            cache.put(root, result);
            LinkedHashMap<String, MetaExpression> values = result.getValue();

            // Parse children
            for (Entry<?, ?> child : ((Map<?, ?>) root).entrySet()) {
                MetaExpression current = parseObject(child.getValue(), cache, metaExpressionDeserializer);
                values.put(child.getKey().toString(), current);
                current.registerReference();
            }

            // Push map
            result.setValue(values);

            return result;
        }

        // No list, no map. This must be an atomic value. No need to cache those since they cannot be circular
        // Boolean
        if (root instanceof Boolean) {
            return ExpressionBuilderHelper.fromValue((Boolean) root);
        }

        // Numbers
        if (root instanceof Number) {
            return ExpressionBuilderHelper.fromValue((Number) root);
        }

        if (root instanceof String) {
            return ExpressionBuilderHelper.fromValue(root.toString());
        }

        if (root instanceof java.util.Date) {
            java.util.Date inputDate = (java.util.Date) root;
            /*
              Here we use getTime because in the case of java.sql.Date the getInstant()
              method throws an exception.
             */
            long time = inputDate.getTime();
            Instant instant = Instant.ofEpochMilli(time);
            Date date = injector.getInstance(DateFactory.class).from(instant);
            MetaExpression result = ExpressionBuilderHelper.fromValue(date.toString());
            result.storeMeta(date);
            return result;
        }

        // Temporary fix for binary data until that is implemented properly.
        if (root instanceof byte[]) {
            return ExpressionBuilderHelper.fromValue("[Binary content]");
        }

        MetaExpression result = metaExpressionDeserializer.parseObject(root);
        if (result != null) {
            return result;
        }

        throw new IllegalArgumentException("Unable to deserialize " + root.getClass().getName());
    }

    public abstract MetaExpression copy();
}
