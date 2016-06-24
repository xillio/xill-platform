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
package nl.xillio.xill.api.construct;

import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * Handles the definition of arguments/parameters that are accepted by a certain construct or custom routine.
 */
public class Argument implements AutoCloseable {

    private final String name;
    private final MetaExpression defaultValue;
    private MetaExpression value;
    private final boolean[] acceptedTypes = new boolean[ExpressionDataType.values().length];
    private final String typeDescription;

    /**
     * Creates a complex argument, which accepts various variable types.
     *
     * @param name          the name of the argument
     * @param acceptedTypes all accepted types
     */
    public Argument(final String name, final ExpressionDataType... acceptedTypes) {
        this(name, null, acceptedTypes);
    }

    /**
     * Creates a simple argument with a default value.
     *
     * @param name          the name of the argument
     * @param defaultValue  the default value
     * @param acceptedTypes the accepted structures for this argument
     */
    public Argument(final String name, final MetaExpression defaultValue, final ExpressionDataType... acceptedTypes) {
        this.name = name;
        this.defaultValue = defaultValue;
        if(defaultValue != null) {
            defaultValue.registerReference();
        }

        if (acceptedTypes.length == 0) {
            // No types provided so accept everything
            for (int i = 0; i < this.acceptedTypes.length; i++) {
                this.acceptedTypes[i] = true;
            }
            typeDescription = "ANY";
        } else {
            // Only accept provided types
            for (ExpressionDataType type : acceptedTypes) {
                this.acceptedTypes[type.toInt()] = true;
            }

            typeDescription = StringUtils.join(
                    Arrays.stream(ExpressionDataType.values())
                            .filter(type -> this.acceptedTypes[type.toInt()])
                            .toArray(),
                    ", ");
        }
    }

    /**
     * Returns the name of the argument.
     *
     * @return the name of the argument
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the default value for this argument.
     *
     * @return the default value for this argument
     */
    public MetaExpression getValue() {
        return value == null ? defaultValue : value;
    }

    /**
     * Sets the value of this argument.
     *
     * @param value the value to set
     * @return whether the value was accepted
     */
    boolean setValue(final MetaExpression value) {
        if (!acceptedTypes[value.getType().toInt()]) {
            return false;
        }
        value.registerReference();
        if(this.value != null) {
            this.value.releaseReference();
        }
        this.value = value;
        return true;
    }

    /**
     * Checks if this argument has been set.
     *
     * @return whether the argument holds a value
     */
    public boolean isSet() {
        return defaultValue != null || value != null;
    }

    /**
     * Resets the argument to its initial state.
     */
    public void clear() {
        if (value != null) {
            value.releaseReference();
        }
        value = null;
    }

    @Override
    public String toString() {
        String nameString = getName();
        if (defaultValue != null) {
            nameString += " = " + defaultValue;
        }
        return "<" + getType() + "> " + nameString;
    }

    /**
     * @return a string description of the types
     */
    public String getType() {

        return typeDescription;
    }

    /**
     * Returns the default values as a string.
     *
     * @return the string representation of the default value, or null if none is set
     */
    public String getDefaultValueAsString() {
        if (defaultValue == null) {
            return null;
        }
        return defaultValue.toString();
    }

    @Override
    public void close() {
        if (defaultValue != null) {
            defaultValue.releaseReference();
        }
        if (value != null) {
            value.releaseReference();
        }
    }
}
