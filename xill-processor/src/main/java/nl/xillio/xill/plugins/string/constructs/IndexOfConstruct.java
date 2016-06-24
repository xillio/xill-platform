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
 * Returns the first index of the needle in the provided text.
 * </p>
 * <p>
 * Optionally an alternative start position can be specified.
 * </p>
 *
 * @author Sander
 */
public class IndexOfConstruct extends Construct {
    @Inject
    private StringUtilityService stringService;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (string1, string2, value) -> process(string1, string2, value, stringService),
                new Argument("string1", ATOMIC),
                new Argument("string2", ATOMIC),
                new Argument("startPos", fromValue(0), ATOMIC));
    }

    static MetaExpression process(final MetaExpression string1, final MetaExpression string2, final MetaExpression value, final StringUtilityService stringService) {
        assertNotNull(string1, "string1");
        assertNotNull(string2, "string2");

        return fromValue(stringService.indexOf(
                string1.getStringValue(), string2.getStringValue(), value.getNumberValue().intValue()));
    }
}
