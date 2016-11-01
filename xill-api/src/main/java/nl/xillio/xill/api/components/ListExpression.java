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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * This class represents a written list in a script e.g. [1,2,3,4].
 * </p>
 * Values:
 * <ul>
 * <li><b>{@link String}: </b> the JSON representation</li>
 * <li><b>{@link Boolean}: </b> {@code false} if the list is {@code null}, {@code true} otherwise (even if empty)</li>
 * <li><b>{@link Number}: </b> {@link Double#NaN}</li>
 * </ul>
 */

class ListExpression extends CollectionExpression {

    private final List<? extends MetaExpression> value;

    /**
     * @param value the value to set
     */
    public ListExpression(final List<MetaExpression> value) {
        this.value = value;

        setValue(value);
        //Register references
        value.forEach(MetaExpression::registerReference);
    }

    @Override
    public Collection<Processable> getChildren() {
        return new ArrayList<>(value);
    }

    @Override
    public ListExpression copyExpression() {
        assertOpen();
        return new ListExpression(value.stream().map(MetaExpression::copy).collect(Collectors.toList()));
    }

    @Override
    public Number getSize() {
        return value.size();
    }
}
