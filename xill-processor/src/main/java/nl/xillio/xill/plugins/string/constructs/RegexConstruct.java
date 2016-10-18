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
import com.google.inject.Singleton;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.string.services.string.RegexService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * <p>
 * Returns a list of matches of the specified regex on the provided string.
 * </p>
 *
 * @author Sander
 */
@Singleton
public class RegexConstruct extends Construct {

    private final RegexService regexService;
    @Inject
    public RegexConstruct(RegexService regexService){
        this.regexService = regexService;
    }


    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("string", ATOMIC),
                new Argument("regex", ATOMIC),
                new Argument("timeout", fromValue(regexService.getRegexTimeout()), ATOMIC));
    }

    @SuppressWarnings("squid:S1166")
    private MetaExpression process(final MetaExpression valueVar, final MetaExpression regexVar, final MetaExpression timeoutVar) {

        String regex = regexVar.getStringValue();
        int timeout = (int) timeoutVar.getNumberValue().doubleValue();

        try {
            Matcher matcher = regexService.getMatcher(regex, valueVar.getStringValue(), timeout);

            if (regexService.matches(matcher)) {
                return fromValue(makeList(matcher));
            }
            return NULL;
        } catch (PatternSyntaxException e) {
            throw new InvalidUserInputException("Invalid pattern in regex().", regex, "A valid regular expression.", "use String;\nvar url = \"http://www.xillio.com/wp-content/uploads/screenshot-ns-website.png\";\n" +
                    "String.regex(url, \"http.*/(.*?)\\\\.(.*?)\");", e);
        } catch (IllegalArgumentException e) {
            throw new RobotRuntimeException("Error while executing the regex", e);
        }
    }

    private List<MetaExpression> makeList(Matcher matcher) {
        List<MetaExpression> list = new ArrayList<>();
        List<String> listAsStrings = regexService.tryMatchElseNull(matcher);
        for (String s : listAsStrings) {
            if (s != null) {
                list.add(fromValue(s));
            } else {
                list.add(NULL);
            }
        }
        return list;
    }

}
