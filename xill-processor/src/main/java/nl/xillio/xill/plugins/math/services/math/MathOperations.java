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
package nl.xillio.xill.plugins.math.services.math;

import com.google.inject.ImplementedBy;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.math.MathXillPlugin;

/**
 * This interface represents some of the operations for the {@link MathXillPlugin}
 */
@ImplementedBy(MathOperationsImpl.class)
public interface MathOperations {

    /**
     * Get the absolute value
     *
     * @param value the value
     * @return the absolute value
     */
    double abs(Number value);

    /**
     * Round a number to it's nearest integer
     *
     * @param value the value
     * @return the rounded integer or long
     */
    long round(Number value);

    /**
     * Returns a random double between 0 and 1.
     *
     * @return A random double between 0 and 1.
     */
    double random();

    /**
     * Returns a random long between 0 and a given value.
     *
     * @param value The max value.
     * @return A long between 0 and max value.
     */
    long random(long value);

    /**
     * Returns true or false depending on if the given value is a number or not.
     *
     * @param value The given object to check.
     * @return A boolean which is true if value is a number, otherwise false.
     */
    boolean isNumber(MetaExpression value);

    /**
     * Get the largest number smaller than or equal to the value
     *
     * @param value the value
     * @return the largest previous number
     */
    long floor(Number value);

    /**
     * Get the smallest number larger than or equal to the value
     *
     * @param value the value
     * @return the smallest following number
     */
    long ceiling(Number value);
}
