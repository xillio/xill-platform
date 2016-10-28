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
 * This class represents the behavior of a number.
 * </p>
 * Values:
 * <ul>
 * <li><b>{@link String}: </b> the actual value as a string</li>
 * <li><b>{@link Boolean}: </b> if value == 0 then false else true</li>
 * <li><b>{@link Number}: </b> the acual value</li>
 * </ul>
 */
class NumberBehavior extends AbstractBehavior {

    private final Number value;

    /**
     * Default creator.
     *
     * @param value the value to set
     */
    public NumberBehavior(final Number value) {
        this.value = value;
    }

    @Override
    public Number getNumberValue() {
        return value;
    }

    @Override
    public String getStringValue() {
        return value.toString();
    }

    @Override
    public boolean getBooleanValue() {
        // NumberBehavior cannot be NULL or null by design, but might in the future
        if (value == null || isNull()) {
            return false;
        }

        Double doubleValue = value.doubleValue();

        return !(Double.isNaN(doubleValue) || doubleValue.equals(0.0d));
    }

    @Override
    public NumberBehavior copy() {
        return new NumberBehavior(value);
    }
}
