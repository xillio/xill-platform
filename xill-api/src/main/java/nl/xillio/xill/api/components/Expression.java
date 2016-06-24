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

/**
 * This interface represents a language component that can hold a value.
 * Every expression should have all three types: {@link Boolean}, {@link String} and {@link Number}.
 *
 * @see BooleanBehavior
 * @see NumberBehavior
 * @see StringBehavior
 */
public interface Expression extends AutoCloseable {

    /**
     * @return the number representation of the expression
     */
    Number getNumberValue();

    /**
     * @return the string representation of the expression
     */
    String getStringValue();

    /**
     * @return the boolean representation of the expression
     */
    boolean getBooleanValue();

    /**
     * @return {@code true} if and only if the expression is considered null
     */
    boolean isNull();

    /**
     * @return the IOStream representation of the expression
     */
    IOStream getBinaryValue();

    default void close() {
    }
}
