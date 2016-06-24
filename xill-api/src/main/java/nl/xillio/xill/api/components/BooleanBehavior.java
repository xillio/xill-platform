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
 * This class represents the behavior of a boolean.
 * </p>
 * Values:
 * <ul>
 * <li><b>{@link String}: </b> true or false</li>
 * <li><b>{@link Boolean}: </b> original value</li>
 * <li><b>{@link Number}: </b> if value equals true then 1 else 0</li>
 * </ul>
 */
class BooleanBehavior extends AbstractBehavior {

    private final boolean value;

    /**
     * Default constructor.
     *
     * @param value the value to set the boolean to
     */
    public BooleanBehavior(final boolean value) {
        this.value = value;
    }

    @Override
    public Number getNumberValue() {
        return value ? 1 : 0;
    }

    @Override
    public String getStringValue() {
        return Boolean.toString(value);
    }

    @Override
    public boolean getBooleanValue() {
        return value;
    }

}
