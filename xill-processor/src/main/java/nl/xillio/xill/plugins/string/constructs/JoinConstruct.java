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
package nl.xillio.xill.plugins.string.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.NotImplementedException;
import nl.xillio.xill.plugins.string.services.string.StringUtilityService;

import java.util.List;
import java.util.Map;

/**
 * Concatenates a list of elements using a delimiter
 */
public class JoinConstruct extends Construct {
    private final StringUtilityService stringService;
    @Inject
    public JoinConstruct(StringUtilityService stringService){
        this.stringService = stringService;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("list"),
                new Argument("delimiter", fromValue(""), ATOMIC));
    }

    private MetaExpression process(final MetaExpression list, final MetaExpression delimiter) {
        String output;

        switch (list.getType()) {
            case ATOMIC:
                output = list.getStringValue();
                break;
            case LIST:
                @SuppressWarnings("unchecked")
                String[] stringList = ((List<MetaExpression>) list.getValue()).stream().map(MetaExpression::getStringValue).toArray(i -> new String[i]);
                output = stringService.join(stringList, delimiter.getStringValue());
                break;
            case OBJECT:
                @SuppressWarnings("unchecked")
                String[] stringObject = ((Map<String, MetaExpression>) list.getValue()).values().stream().map(MetaExpression::getStringValue).toArray(i -> new String[i]);
                output = stringService.join(stringObject, delimiter.getStringValue());
                break;
            default:
                throw new NotImplementedException("This type has not been implemented.");
        }

        return fromValue(output);
    }
}
