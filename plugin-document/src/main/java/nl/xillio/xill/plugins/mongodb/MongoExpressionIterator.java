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
package nl.xillio.xill.plugins.mongodb;

import com.mongodb.MongoCursorNotFoundException;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.MetaExpressionIterator;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Specific iterator to iterate through MongoDB expressions
 *
 * @author Edward van Egdom
 */
public class MongoExpressionIterator<E> extends MetaExpressionIterator<E> {
    public MongoExpressionIterator(Iterator<E> source, Function<E, MetaExpression> transformer) {
        super(source, transformer);
    }

    @Override
    public boolean hasNext() {
        try {
            return super.hasNext();
        } catch (MongoCursorNotFoundException e) {
            throw new RobotRuntimeException("Iteration failed: Mongo cursor was not found", e);
        }
    }

    @Override
    public MetaExpression next() {
        try {
            return super.next();
        } catch (MongoCursorNotFoundException e) {
            throw new RobotRuntimeException("Iteration failed: Mongo cursor was not found", e);
        }
    }
}

