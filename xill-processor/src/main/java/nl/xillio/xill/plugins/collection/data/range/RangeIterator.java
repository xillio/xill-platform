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
import java.util.NoSuchElementException;

/**
 * This class is the implementation of the range iterator using Numbers
 *
 * @author Pieter Soels
 */
public class RangeIterator implements Iterator<Number> {
    private final Number end;
    private Number step;
    private Number nextValue;

    public RangeIterator(Number start, Number end, Number step) {
        this.end = end;
        this.step = step;
        nextValue = start;
    }

    @Override
    public boolean hasNext() {
        // If the step is negative, its signum will return -1
        // If the step is positive, its signum will return 1
        // Respectively:
        // end - next will return -1 if end < next (there is a next value if step is negative)
        // end - next will return 1 if end > next (there is a next value if step is positive)
        double signumNextValue = Math.signum(MathUtils.subtract(end, nextValue).doubleValue());
        double signumStep = Math.signum(step.doubleValue());
        return MathUtils.compare(signumNextValue, signumStep) == 0;
    }

    @Override
    public synchronized Number next() {
        if (!hasNext()) {
            throw new NoSuchElementException("There are no elements in the iterator left.");
        }
        Number value = nextValue;
        nextValue = MathUtils.add(nextValue, step);
        return value;
    }
}
