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
class RangeIterator implements Iterator<Number> {
    private final Number end;
    private Number step;
    private Number start;
    private Number counter;

    RangeIterator(Number start, Number end, Number step) {
        this.end = end;
        this.step = step;
        this.start = start;
        counter = 0;
    }

    @Override
    public boolean hasNext() {
        Number absoluteDiff = MathUtils.abs(MathUtils.subtract(end, start));
        Number size = MathUtils.divide(absoluteDiff, MathUtils.abs(step));
        return MathUtils.compare(counter, size) < 0;
    }

    @Override
    public synchronized Number next() {
        if (!hasNext()) {
            throw new NoSuchElementException("There are no elements in the iterator left.");
        }

        Number output = MathUtils.add(start, MathUtils.multiply(counter, step));
        counter = MathUtils.add(counter, 1);
        return output;
    }
}
