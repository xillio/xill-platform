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
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.string.services.string.RegexService;
import nl.xillio.xill.plugins.string.services.string.StringUtilityService;

import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * Returns a new string in which occurrences of regex needle found in the text
 * have been replaced by the replacement string. If the parameter 'replaceAll'
 * is set to false, the routine will only replace the first occurrence. If the
 * parameter 'useRegex' is set to false, the routine will not use a regex.
 *
 * @author Sander
 */
public class ReplaceConstruct extends Construct {

    private final RegexService regexService;
    private final StringUtilityService stringService;

    @Inject
    public ReplaceConstruct(RegexService regexService, StringUtilityService stringService) {
        this.regexService = regexService;
        this.stringService = stringService;
    }

    @Override
    @SuppressWarnings("squid:S2095")
    // Suppress "Resources should be closed": Arguments do not need to be closed here, because ConstructProcessor closes them
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        Argument[] args = {
                new Argument("text", ATOMIC),
                new Argument("needle", ATOMIC),
                new Argument("replacement", ATOMIC),
                new Argument("useRegex", TRUE, ATOMIC),
                new Argument("replaceAll", TRUE, ATOMIC),
                new Argument("timeout", fromValue(regexService.getRegexTimeout()), ATOMIC)};

        return new ConstructProcessor(this::process, args);

    }

    @SuppressWarnings("squid:S1166")
    private MetaExpression process(final MetaExpression[] input) {

        for (int i = 0; i < 5; i++) {
            assertNotNull(input[i], "input");
        }

        String text = input[0].getStringValue();
        String needle = input[1].getStringValue();
        String replacement = input[2].getStringValue();
        boolean useregex = input[3].getBooleanValue();
        boolean replaceall = input[4].getBooleanValue();
        int timeout = (int) input[5].getNumberValue().doubleValue();

        if (useregex) {
            try {
                Matcher m = regexService.getMatcher(needle, text, timeout);
                if (replaceall) {
                    return fromValue(regexService.replaceAll(m, replacement));
                }
                return fromValue(regexService.replaceFirst(m, replacement));
            } catch (PatternSyntaxException e) {
                throw new InvalidUserInputException("Invalid pattern in regex().", needle, "A valid regular expression.", "use String;\nString.replace(\"The quick brown fox.\", \"fox\", \"dog\");", e);
            } catch (IllegalArgumentException e) {
                throw new RobotRuntimeException("Error while executing the regex", e);
            }
        }
        if (replaceall) {
            return fromValue(stringService.replaceAll(text, needle, replacement));
        }
        return fromValue(stringService.replaceFirst(text, needle, replacement));
    }
}
