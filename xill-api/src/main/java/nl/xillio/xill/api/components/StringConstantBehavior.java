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
 * <p>
 * This class represents the behavior of a string constant.
 * </p>
 * <p>
 * Values:
 * <ul>
 * <li><b>{@link String}: </b> actual value</li>
 * <li><b>{@link Boolean}: </b> if the value is null or empty then false otherwise true</li>
 * <li><b>{@link Number}: </b> {@link Double#NaN}</li>
 * </ul>
 */
class StringConstantBehavior extends StringBehavior {

    /**
     * Default constructor.
     *
     * @param value the value to set
     */
    public StringConstantBehavior(final String value) {
        super(value);
    }

    @Override
    public Number getNumberValue() {
        return Double.NaN;
    }
}
