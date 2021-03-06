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
 * Returns the last index of the needle in the provided text.
 * </p>
 * @author Daan Knoope
 */
public class LastIndexOfConstruct extends Construct{

    private final StringUtilityService stringUtilityService;

    @Inject
    public LastIndexOfConstruct(StringUtilityService stringUtilityService) {this.stringUtilityService = stringUtilityService;}

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("haystack", ATOMIC),
                new Argument("needle", ATOMIC)
        );
    }

    private MetaExpression process(final MetaExpression haystack, final MetaExpression needle){
        assertNotNull(haystack, "haystack");
        assertNotNull(needle, "needle");

        return fromValue(stringUtilityService.lastIndexOf(haystack.getStringValue(), needle.getStringValue()));
    }
}
