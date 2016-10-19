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
import nl.xillio.xill.plugins.string.services.string.RegexService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * <p>
 * Extracts all matching subStrings of text into a list.
 * </p>
 *
 * @author Sander
 */
public class AllMatchesConstruct extends Construct {

    private final RegexService regexService;

    @Inject
    public AllMatchesConstruct(RegexService regexService) {
        this.regexService = regexService;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (valueVar, regexVar, timeout) -> process(valueVar, regexVar, timeout),
                new Argument("value", ATOMIC),
                new Argument("regex", ATOMIC),
                new Argument("timeout", fromValue(regexService.getRegexTimeout()), ATOMIC));
    }

    private MetaExpression process(final MetaExpression textVar, final MetaExpression regexVar, final MetaExpression timeoutVar) {

        List<MetaExpression> list = new ArrayList<>();

        String text = textVar.getStringValue();
        String regex = regexVar.getStringValue();
        int timeout = (int) timeoutVar.getNumberValue().doubleValue();

        try {
            Matcher matcher = regexService.getMatcher(regex, text, timeout);
            List<String> results = regexService.tryMatch(matcher);
            for (String s : results) {
                list.add(fromValue(s));
            }
        } catch (PatternSyntaxException e) {
            throw new InvalidUserInputException("Invalid pattern: " + e.getMessage(), regex, "A valid regular expression.", "use String;\n" +
                    "var s = \"abc def ghi jkl. Mno\";\n" +
                    "String.allMatches(s, \"\\\\w+\");", e);
        } catch (IllegalArgumentException e) {
            throw new InvalidUserInputException("Illegal argument: " + e.getMessage(), text, "A valid text value.", "use String;\n" +
                    "var s = \"abc def ghi jkl. Mno\";\n" +
                    "String.allMatches(s, \"\\\\w+\");", e);
        }
        return fromValue(list);
    }
}
