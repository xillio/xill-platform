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

import com.google.inject.Singleton;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.NotImplementedException;

/**
 * This is the main implementation of {@link MathOperations}
 */
@Singleton
public class MathOperationsImpl implements MathOperations {

    @Override
    public double abs(final Number value) {

        if (value instanceof Integer) {
            return Math.abs(value.intValue());
        } else if (value instanceof Long) {
            return Math.abs(value.longValue());
        } else if (value instanceof Float) {
            return Math.abs(value.floatValue());
        } else {
            return Math.abs(value.doubleValue());
        }
    }

    @Override
    public long round(final Number value) {
        if (value instanceof Integer) {
            return value.intValue();
        } else if (value instanceof Long) {
            return value.longValue();
        } else if (value instanceof Float) {
            return Math.round(value.floatValue());
        } else {
            return Math.round(value.doubleValue());
        }
    }

    @Override
    public double random() {
        return Math.random();

    }

    @Override
    public long random(final long value) {
        return (long) (Math.random() * value);
    }

    @Override
    public boolean isNumber(final MetaExpression value) {
        switch (value.getType()) {
            case ATOMIC:
                return !value.isNull() && !Double.isNaN(value.getNumberValue().doubleValue());
            case LIST:
            case OBJECT:
                return false;
            default:
                throw new NotImplementedException("This type has not been implemented.");
        }
    }

    @Override
    public long floor(Number value) {
        return (long) Math.floor(value.doubleValue());
    }

    @Override
    public long ceiling(Number value) {
        return (long) Math.ceil(value.doubleValue());
    }
}
