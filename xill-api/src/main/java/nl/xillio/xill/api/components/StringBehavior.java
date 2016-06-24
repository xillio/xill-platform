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

import nl.xillio.util.MathUtils;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * <p>
 * This class represents the behavior of a string.
 * </p>
 * <p>
 * Values:
 * <ul>
 * <li><b>{@link String}: </b> actual value</li>
 * <li><b>{@link Boolean}: </b> {@code false} if the value is null or empty, {@code true} otherwise</li>
 * <li><b>{@link Number}: </b> if the value is a number then that number as a {@link Double}, otherwise {@link Double#NaN}</li>
 * </ul>
 */
class StringBehavior extends AbstractBehavior {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("((-?\\d*\\.\\d+(E[-\\+]\\d+)?)|(-?\\d+))");
    private Number cachedNumber;
    private final String value;

    /**
     * Default constructor.
     *
     * @param value the value to set
     */
    public StringBehavior(String value) {
        Objects.requireNonNull(value);
        this.value = value;
    }

    @Override
    public Number getNumberValue() {
        if (cachedNumber == null) {
            if (value == null || value.isEmpty() || !NUMBER_PATTERN.matcher(value).matches()) {
                cachedNumber = Double.NaN;
            } else {
                cachedNumber = MathUtils.parse(value);
            }
        }

        return cachedNumber;
    }

    @Override
    public String getStringValue() {
        return value;
    }

    @Override
    public boolean getBooleanValue() {
        return !value.isEmpty();
    }
}
