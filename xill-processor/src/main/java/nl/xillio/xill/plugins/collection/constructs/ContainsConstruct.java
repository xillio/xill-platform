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
import nl.xillio.xill.api.errors.NotImplementedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.util.List;
import java.util.Map;

/**
 * Returns true if the value is contained in the given list or object otherwise false.
 *
 * @author Sander Visser
 */
public class ContainsConstruct extends Construct {

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                ContainsConstruct::process,
                new Argument("collection", LIST, OBJECT),
                new Argument("value", ATOMIC));
    }

    /**
     * Returns true if the value is contained in the given list or object. Otherwise false.
     *
     * @param input the list or object.
     * @param value the value that is going to be checked.
     * @return true if the list or object contains the value.
     */
    static MetaExpression process(final MetaExpression input, final MetaExpression value) {
        boolean result;
        switch (input.getType()) {
            case OBJECT:
                Map<String, MetaExpression> m = input.getValue();
                try {
                    result = m.containsValue(value);
                } catch (ClassCastException | NullPointerException e) {
                    throw new RobotRuntimeException("The value handed was no valid MetaExpression", e);
                }
                break;
            case LIST:
                List<MetaExpression> l = input.getValue();
                try {
                    result = l.contains(value);
                } catch (ClassCastException | NullPointerException e) {
                    throw new RobotRuntimeException("The value handed was no valid MetaExpression", e);
                }
                break;
            default:
                throw new NotImplementedException("This type is not implemented.");

        }

        return fromValue(result);
    }
}
