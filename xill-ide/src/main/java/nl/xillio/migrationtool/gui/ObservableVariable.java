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
package nl.xillio.migrationtool.gui;

import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.NotImplementedException;

/**
 * This class represents a viewable variable in the {@link VariablePane}
 */
public class ObservableVariable {
    private final String name;
    private final MetaExpression value;
    private final Object source;

    /**
     * Create a new {@link ObservableVariable}
     *
     * @param name
     * @param value
     * @param source
     */
    public ObservableVariable(final String name, final MetaExpression value, final Object source) {
        this.name = name;
        this.value = value;
        this.source = source;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        if (value == null) {
            return ExpressionBuilderHelper.NULL.toString();
        }

        return expressionToString(value);
    }

    /**
     * @return the source
     */
    public Object getSource() {
        return source;
    }

    public MetaExpression getExpression() {
        return value;
    }

    /**
     * Utility function to get a string representation of the provided MetaExpression
     * @param value The MetaExpression to be parsed
     * @return String representation of the meta expression
     */
    public static String expressionToString(MetaExpression value) {
        try {
            switch (value.getType()) {
                case ATOMIC:
                    return value.toString();
                case LIST:
                    return "List [" + value.getSize() + "]";
                case OBJECT:
                    return "Object [" + value.getSize() + "]";
                default:
                    throw new NotImplementedException("This type has not been implemented in the VariablePane");
            }
        } catch (IllegalStateException e) {
            // CTC-1892 - Catch rare expression-already-closed exception
            return e.getMessage();
        }
    }
}
