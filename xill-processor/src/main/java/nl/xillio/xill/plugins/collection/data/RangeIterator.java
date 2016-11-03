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
package nl.xillio.xill.plugins.collection.data;

import nl.xillio.util.MathUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Pieter Soels
 */
public class RangeIterator implements Iterator<Number> {
    private final Number end;
    private Number step;
    private Number nextValue;

    public RangeIterator(Number start, Number end) {
        this(start, end, null);
    }

    public RangeIterator(Number start, Number end, Number step) {
        this.end = end;

        if (MathUtils.compare(start, end) == 0) {
            throw new IllegalArgumentException();
        }

        if (step == null) {
            if (MathUtils.compare(start, end) < 0) {
                step = 1;
            } else if (MathUtils.compare(end, start) < 0) {
                step = -1;
            }
        } else if (MathUtils.compare(Math.ulp(step.doubleValue()), Double.MIN_VALUE) == 0) {
            throw new IllegalArgumentException();
        } else if (MathUtils.compare(start, end) < 0 && MathUtils.compare(step, 0) < 0) {
            throw new IllegalArgumentException();
        } else if (MathUtils.compare(end, start) < 0 && MathUtils.compare(step, 0) > 0) {
            throw new IllegalArgumentException();
        }

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
        return Math.signum(MathUtils.subtract(end, nextValue).doubleValue()) == Math.signum(step.doubleValue());
    }

    @Override
    public synchronized Number next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Number value = nextValue;
        nextValue = MathUtils.add(nextValue, step);
        return value;
    }
}
