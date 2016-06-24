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
import nl.xillio.xill.plugins.collection.services.sort.Sort;

/**
 * returns the sorted list.
 * <p>
 * if recursive is true it will also sort lists inside the list.
 * <p>
 * if onKeys is true it will sort by key.
 *
 * @author Sander Visser
 */
public class SortConstruct extends Construct {

    @Inject
    private Sort sort;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (list, recursive, onKeys, reverse) -> process(list, recursive, onKeys, reverse, sort),
                new Argument("collection", LIST, OBJECT),
                new Argument("recursive", FALSE, ATOMIC),
                new Argument("onKeys", FALSE, ATOMIC),
                new Argument("reverse", FALSE, ATOMIC));
    }

    static MetaExpression process(final MetaExpression inputList, final MetaExpression recursive, final MetaExpression onKeys, final MetaExpression reverse, final Sort sort) {

        boolean sortRecursive = recursive.getBooleanValue();
        boolean sortKeys = onKeys.getBooleanValue();
        boolean lowFirst = reverse.getBooleanValue();
        Object obj = extractValue(inputList);
        obj = sort.asSorted(obj, sortRecursive, sortKeys, lowFirst);
        MetaExpression m = MetaExpression.parseObject(obj);
        return m;

    }
}
