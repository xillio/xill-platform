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

import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * <p>
 * This class represents a written object in a script e.g. { "keyValue": 6 }.
 * </p>
 * <p>
 * Values:
 * <ul>
 * <li><b>{@link String}: </b> a JSON representation</li>
 * <li><b>{@link Boolean}: </b> false if the object is null else true</li>
 * <li><b>{@link Number}: </b> {@link Double#NaN}</li>
 * </ul>
 *
 */
class ObjectExpression extends CollectionExpression {

    private final LinkedHashMap<String, MetaExpression> value;

    /**
     * @param object the value to set
     */
    @SuppressWarnings("squid:S1319")
    // We should use LinkedHashMap as a parameter here to enforce ordering in the map
    ObjectExpression(final LinkedHashMap<String, MetaExpression> object) {
        value = object;

        setValue(value);
        object.values().forEach(MetaExpression::registerReference);
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) throws RobotRuntimeException {
        return InstructionFlow.doResume(this);
    }

    @Override
    public Collection<Processable> getChildren() {
        return new ArrayList<>(value.values());
    }

    @Override
    public Number getNumberValue() {
        return Double.NaN;
    }

    @Override
    public Number getSize(){
        return value.size();
    }

}
