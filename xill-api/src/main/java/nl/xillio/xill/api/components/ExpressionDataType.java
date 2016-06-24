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

/**
 * This enum represents the type of variables.
 */
public enum ExpressionDataType {
    /**
     * Atomic value (Number, String, Boolean).
     */
    ATOMIC,
    /**
     * A list of values.
     */
    LIST,
    /**
     * An object with named fields.
     */
    OBJECT;

    /**
     * @return the int identifier of this type
     */
    public int toInt() {
        return this.ordinal();
    }

    private static ExpressionDataType[] values = values();

    /**
     * Returns the expression datatype corresponding to the provided ordinal.
     *
     * @param ordinal the ordinal
     * @return one of {@link #ATOMIC}, {@link #LIST}, {@link #OBJECT}
     */
    public static ExpressionDataType get(int ordinal) {
        return values[ordinal];
    }


}
