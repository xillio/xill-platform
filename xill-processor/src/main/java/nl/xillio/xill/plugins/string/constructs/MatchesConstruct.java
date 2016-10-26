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

import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * Returns whether the provided value matches the specified regex.
 *
 * @author Sander
 */
public class MatchesConstruct extends Construct {

    private final RegexService regexService;

    @Inject
    public MatchesConstruct(RegexService regexService) {
        this.regexService = regexService;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("value", ATOMIC),
                new Argument("regex", ATOMIC),
                new Argument("timeout", fromValue(regexService.getRegexTimeout()), ATOMIC));
    }

    @SuppressWarnings("squid:S1166")
    private MetaExpression process(final MetaExpression valueVar, final MetaExpression regexVar, final MetaExpression timeoutVar) {
        String value = valueVar.getStringValue();
        String regex = regexVar.getStringValue();

        int timeout = (int) timeoutVar.getNumberValue().doubleValue();

        try {
            Matcher matcher = regexService.getMatcher(regex, value, timeout);
            return fromValue(regexService.matches(matcher));
        } catch (PatternSyntaxException p) {
            throw new InvalidUserInputException("Invalid pattern in regex().", regex, "A valid regular expression.", "use String;\nString.matches(\"I need help!\", \".*help.*\");", p);
        } catch (IllegalArgumentException e) {
            throw new RobotRuntimeException("Illegal argument given.", e);
        }

    }
}
