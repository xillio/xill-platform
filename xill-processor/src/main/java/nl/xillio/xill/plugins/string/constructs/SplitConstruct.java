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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Splits the provided value into a list of strings, based on the provided delimiter.
 * </p>
 * <p>
 * Optionally you can set keepempty to true to keep empty entries.
 * </p>
 *
 * @author Sander
 */
public class SplitConstruct extends Construct {
    @Inject
    private StringUtilityService stringService;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (string, delimiter, keepEmpty) -> process(string, delimiter, keepEmpty, stringService),
                new Argument("string", ATOMIC),
                new Argument("delimiter", ATOMIC),
                new Argument("keepEmpty", FALSE, ATOMIC));
    }

    static MetaExpression process(final MetaExpression string, final MetaExpression delimiter, final MetaExpression keepempty, final StringUtilityService stringService) {
        assertNotNull(string, "string");
        assertNotNull(delimiter, "delimiter");

        boolean keepEmpty = keepempty.getBooleanValue();

        String[] stringArray = stringService.split(string.getStringValue(), delimiter.getStringValue());

        List<MetaExpression> list = new ArrayList<>();

        Arrays.stream(stringArray).forEach(str -> {
            if (keepEmpty || !"".equals(str)) {
                list.add(fromValue(str));
            }
        });

        return fromValue(list);
    }
}
