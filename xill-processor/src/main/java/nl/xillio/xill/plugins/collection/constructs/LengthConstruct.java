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

import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * returns the length of the given {@link ExpressionDataType#LIST} or {@link ExpressionDataType#OBJECT}.
 * </p>
 *
 * @author Sander Visser
 */
public class LengthConstruct extends Construct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                list -> process(list),
                new Argument("collection", LIST, OBJECT));
    }

    static MetaExpression process(final MetaExpression input) {

        int elements = 0;

        if (input.getType() == LIST) {
            @SuppressWarnings("unchecked")
            List<MetaExpression> list = (ArrayList<MetaExpression>) input.getValue();
            elements = list.size();

        } else {
            //can suppress warning since the input only accepts LIST or OBJECT.
            @SuppressWarnings("unchecked")
            Map<String, MetaExpression> object = (Map<String, MetaExpression>) input.getValue();
            elements = object.size();
        }
        return fromValue(elements);
    }
}
