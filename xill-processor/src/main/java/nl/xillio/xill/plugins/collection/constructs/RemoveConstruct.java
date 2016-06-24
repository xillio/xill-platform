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
package nl.xillio.xill.plugins.collection.constructs;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.util.List;
import java.util.Map;

/**
 * Removes the element at the given index from the list.
 *
 * @author Sander Visser
 */
public class RemoveConstruct extends Construct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (input, index) -> process(input, index),
                new Argument("collection", LIST, OBJECT),
                new Argument("index", ATOMIC));
    }

    static MetaExpression process(final MetaExpression input, final MetaExpression indexVar) {
        if (input.getType() == LIST) {
            @SuppressWarnings("unchecked")
            List<MetaExpression> list = (List<MetaExpression>) input.getValue();

            int i = indexVar.getNumberValue().intValue();
            if (i >= 0 && i < list.size()) {
                list.remove(i).releaseReference();
            } else {
                throw new RobotRuntimeException("Index is out of bounds: " + i);
            }
        } else if (input.getType() == OBJECT) {
            @SuppressWarnings("unchecked")
            Map<String, MetaExpression> object = (Map<String, MetaExpression>) input.getValue();
            if (object.containsKey(indexVar.getStringValue())) {
                object.remove(indexVar.getStringValue());
            }
        }
        return NULL;

    }
}
