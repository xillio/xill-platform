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
 * Repeats the string the provided number of times.
 *
 * @author Sander
 */
public class RepeatConstruct extends Construct {

    private final StringUtilityService stringService;

    @Inject
    public RepeatConstruct(StringUtilityService stringService) {
        this.stringService = stringService;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("string", ATOMIC),
                new Argument("count", ATOMIC));
    }

    private MetaExpression process(final MetaExpression string, final MetaExpression value) {
        assertNotNull(string, "string");
        assertNotNull(value, "value");

        String repeatedString = stringService.repeat(string.getStringValue(), value.getNumberValue().intValue());

        return fromValue(repeatedString);
    }
}
