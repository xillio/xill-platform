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

import java.util.Map;

public class ContainsKeyConstruct extends Construct {
    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(ContainsKeyConstruct::process,
                new Argument("collection", OBJECT),
                new Argument("key", ATOMIC));
    }

    /**
     * Returns true if the object contains the given key, or false otherwise.
     *
     * @param object The object.
     * @param key    The key to check.
     * @return True if the object contains the key, or false otherwise.
     */
    static MetaExpression process(final MetaExpression object, final MetaExpression key) {
        Map<String, MetaExpression> map = object.getValue();
        return fromValue(map.containsKey(key.getStringValue()));
    }
}
