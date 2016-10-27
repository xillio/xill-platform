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

        if (start.doubleValue() == end.doubleValue()){
            throw new IllegalArgumentException();
        }

        if(step == null) {
            if(start.doubleValue() < end.doubleValue()) {
                step = 1;
            } else if (end.doubleValue() < start.doubleValue()) {
                step = -1;
            }
        } else if (step.doubleValue() == 0) {
            throw new IllegalArgumentException();
        } else if(start.doubleValue() < end.doubleValue() && step.doubleValue() < 0) {
            throw new IllegalArgumentException();
        } else if (end.doubleValue() < start.doubleValue() && step.doubleValue() > 0) {
            throw new IllegalArgumentException();
        }

        this.step = step;
        nextValue = start;
    }

    @Override
    public boolean hasNext() {
        return Math.signum(end.doubleValue() - nextValue.doubleValue()) == Math.signum(step.doubleValue());
    }

    @Override
    public synchronized Number next() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }
        Number value = nextValue;
        nextValue = nextValue.doubleValue() + step.doubleValue();
        return value;
    }
}
