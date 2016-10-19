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
import nl.xillio.xill.plugins.string.services.string.StringUtilityService;

/**
 * <p>
 * Returns true when the first value contains the second value.
 * </p>
 *
 * @author Sander
 */
public class ContainsConstruct extends Construct {

    private final StringUtilityService stringService;

    @Inject
    public ContainsConstruct(StringUtilityService stringService) {
        this.stringService = stringService;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("haystack", ATOMIC, LIST),
                new Argument("needle", ATOMIC));
    }

    private MetaExpression process(final MetaExpression haystack, final MetaExpression needle) {
        // If either is null then false.
        if (haystack == NULL || needle == NULL) {
            return fromValue(false);
        }

        // Compare strings
        String value1 = haystack.getStringValue();
        String value2 = needle.getStringValue();
        return fromValue(stringService.contains(value1, value2));
    }

}
