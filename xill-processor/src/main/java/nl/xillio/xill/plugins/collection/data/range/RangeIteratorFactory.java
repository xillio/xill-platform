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
package nl.xillio.xill.plugins.collection.data.range;

import nl.xillio.util.MathUtils;

import java.util.Iterator;

/**
 * This class is responsible for validating the input and initializing the iterator (Factory).
 *
 * @author Pieter Soels
 */
public class RangeIteratorFactory {
    public Iterator<Number> createIterator(Number start, Number end, Number step) {
        Number sanitizedStep = retrieveStep(start, end, step);
        verifyInput(start, end, sanitizedStep);
        return new RangeIterator(start, end, sanitizedStep);
    }

    private Number retrieveStep(Number start, Number end, Number step) {
        if (step == null) {
            if (MathUtils.compare(start, end) < 0) {
                return 1;
            } else if (MathUtils.compare(end, start) < 0) {
                return -1;
            }
            return 0;
        }

        return step;
    }

    private void verifyInput(Number start, Number end, Number step) {
        assertCondition(
                MathUtils.compare(start, end) != 0,
                "The start-value and end-value must not be equal to each other.");

        assertCondition(
                MathUtils.compare(Math.ulp(step.doubleValue()), Double.MIN_VALUE) != 0,
                "The step-value must not be equal to zero.");

        assertCondition(
                MathUtils.compare(start, end) > 0 || MathUtils.compare(step, 0) > 0,
                "The step-value must not be negative when the start-value is lower than the end-value.");

        assertCondition(
                MathUtils.compare(start, end) < 0 || MathUtils.compare(step, 0) < 0,
                "The step-value must not be positive when the start-value is greater than the end-value.");
    }

    private void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
