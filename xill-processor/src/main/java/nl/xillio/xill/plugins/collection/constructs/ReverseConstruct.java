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
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.collection.services.reverse.Reverse;

/**
 * <p>
 * returns the reverse of the given {@link ExpressionDataType#LIST} or {@link ExpressionDataType#OBJECT}.
 * </p>
 * <p>
 * If recursive is true it will also reverse lists and objects inside the given list or object.
 * </p>
 *
 * @author Sander Visser
 */
public class ReverseConstruct extends Construct {

    @Inject
    private Reverse reverse;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (list, recursive) -> process(list, recursive, reverse),
                new Argument("collection", LIST, OBJECT),
                new Argument("recursive", FALSE, ATOMIC));
    }

    static MetaExpression process(final MetaExpression input, final MetaExpression recursiveVar, final Reverse reverse) {

        boolean reverseRecursive = recursiveVar.getBooleanValue();

        Object obj = extractValue(input);
        obj = reverse.asReversed(obj, reverseRecursive);
        return MetaExpression.parseObject(obj);

    }
}
