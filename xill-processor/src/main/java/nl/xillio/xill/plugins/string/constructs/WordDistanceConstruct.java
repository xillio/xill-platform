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
 * Returns a number between 0 (no likeness) and 1 (identical), indicating how much the two strings are alike.
 * </p>
 * <p>
 * If the option 'relative' is set to false, then the absolute editdistance will be returned rather than a relative distance.
 * </p>
 *
 * @author Sander
 */
public class WordDistanceConstruct extends Construct {

    private StringUtilityService stringService;
    @Inject
    public WordDistanceConstruct(StringUtilityService stringService){
        this.stringService = stringService;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {

        return new ConstructProcessor(
                (source, target, relative) -> process(source, target, relative),
                new Argument("source", ATOMIC),
                new Argument("target", ATOMIC),
                new Argument("relative", TRUE));
    }

    private MetaExpression process(final MetaExpression source, final MetaExpression target, final MetaExpression relative) {
        assertNotNull(source, "source");
        assertNotNull(target, "target");

        int edits = stringService.damlev(source.getStringValue(), target.getStringValue());

        if (!relative.getBooleanValue()) {
            return fromValue(edits);
        }

        int maxlength = Math.max(source.getStringValue().length(), target.getStringValue().length());
        double similarity = 1 - (double) edits / maxlength;
        return fromValue(similarity);
    }

}
