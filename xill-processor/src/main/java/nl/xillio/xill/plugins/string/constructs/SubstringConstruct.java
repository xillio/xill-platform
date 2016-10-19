/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *//**
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
 */package nl.xillio.xill.plugins.string.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.string.services.string.StringUtilityService;

/**
 * <p>
 * Returns the substring of text between position start and position end.
 * </p>
 * <p>
 * If the end position equals 0 it will take the full length of the string.
 * </p>
 * <p>
 * The start position is set to 0 if the end position is smaller than the start position.
 * </p>
 *
 * @author Sander
 */
public class SubstringConstruct extends Construct {

    public final StringUtilityService stringService;

    @Inject
    public SubstringConstruct(StringUtilityService stringService) {
        this.stringService = stringService;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("string", ATOMIC),
                new Argument("start", ATOMIC),
                new Argument("end", fromValue(0), ATOMIC));
    }

    @SuppressWarnings("squid:S1166")
    private MetaExpression process(final MetaExpression string, final MetaExpression startVar, final MetaExpression endVar) {
        assertNotNull(string, "string");
        assertNotNull(startVar, "start");
        assertNotNull(endVar, "end");

        String text = string.getStringValue();
        int start = startVar.getNumberValue().intValue();
        int end = endVar.getNumberValue().intValue();

        // Special case; If end equals 0, then take the full length of the
        // string.
        if (end == 0) {
            end = text.length();
        }

        // If end is smaller than start, then start is basically invalid. Assume
        // start = 0
        if (end < start) {
            start = 0;
        }

        try {
            return fromValue(stringService.subString(text, start, end));
        } catch (StringIndexOutOfBoundsException e) {
            throw new RobotRuntimeException("Index out of bounds: " + e.getMessage(), e);
        }

    }
}
