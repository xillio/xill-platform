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

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.collection.services.duplicate.Duplicate;

/**
 * Returns a deep copy of the given list or object.
 *
 * @author Sander Visser
 */
public class DuplicateConstruct extends Construct {

    @Inject
    private Duplicate duplicate;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                input -> process(input, duplicate),
                new Argument("collection", LIST, OBJECT));
    }

    /**
     * Returns a deep copy of the given list or object.
     *
     * @param input the list or object.
     * @return the deep copy of the list or object.
     */
    static MetaExpression process(final MetaExpression input, final Duplicate duplicate) {
        Object obj = extractValue(input);
        obj = duplicate.duplicate(obj);
        MetaExpression output = MetaExpression.parseObject(obj);
        return output;

    }
}
