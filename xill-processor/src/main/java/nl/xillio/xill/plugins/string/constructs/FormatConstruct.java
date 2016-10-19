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
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.string.services.string.RegexService;
import nl.xillio.xill.plugins.string.services.string.StringUtilityService;

import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * <p>
 * Formats the string with the provided values.
 * </p>
 * <p>
 * Does not support Time/Date.
 * </p>
 *
 * @author Sander
 */
public class FormatConstruct extends Construct {

    private static final String EXPLANATION = "use String;\nString.format(\"%3$2s %1$2s %1$2s %2$2s\" , [\"a\", \"b\", \"c\"] );";
    private final RegexService regexService;
    private final StringUtilityService stringService;

    /**
     * Create a new {@link FormatConstruct}
     */
    @Inject
    public FormatConstruct(RegexService regexService, StringUtilityService stringService) {
        this.regexService = regexService;
        this.stringService = stringService;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("text", ATOMIC),
                new Argument("values", LIST));
    }

    private MetaExpression process(final MetaExpression textVar, final MetaExpression valueVar) {
        assertNotNull(textVar, "text");


        List<MetaExpression> formatList = formatText(textVar);

        List<Object> castedList = castMetaExpressions(formatList, valueVar);

        try {
            return fromValue(stringService.format(textVar.getStringValue(), castedList));
        } catch (MissingFormatArgumentException e) {
            throw new InvalidUserInputException("Not enough arguments: " + e.getMessage(), valueVar.getStringValue(), "A correct list of arguments.", EXPLANATION, e);
        } catch (IllegalFormatException e) {
            throw new InvalidUserInputException("Illegal format handed: " + e.getMessage(), textVar.getStringValue(), "A valid format specifier.", EXPLANATION, e);
        }
    }


    private List<MetaExpression> formatText(MetaExpression textVar) {
        List<MetaExpression> formatList = new ArrayList<>();
        try {
            Matcher matcher = regexService.getMatcher("%[[^a-zA-Z%]]*([a-zA-Z]|[%])", textVar.getStringValue(), regexService.getRegexTimeout());
            List<String> tryFormat = regexService.tryMatch(matcher);
            for (String s : tryFormat) {
                formatList.add(fromValue(s));
            }
        } catch (PatternSyntaxException e) {
            throw new RobotRuntimeException("SyntaxError in the system provided pattern: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RobotRuntimeException("Illegal argument handed when trying to match: " + e.getMessage(), e);
        }
        return formatList;
    }


    private List<Object> castMetaExpressions(List<MetaExpression> formatList, final MetaExpression valueVar) {
        @SuppressWarnings("unchecked")
        List<MetaExpression> numberList = (List<MetaExpression>) valueVar.getValue();
        List<Object> list = new ArrayList<>();
        int count = 0;
        String typeString;
        for (int j = 0; j < numberList.size() - count; j++) {
            if (j >= formatList.size()) {
                break;
            }
            typeString = formatList.get(j).getStringValue();
            switch (typeString.charAt(typeString.length() - 1)) {
                case 'd':
                case 'o':
                case 'x':
                case 'X':
                case 'h':
                case 'H':
                    list.add(numberList.get(j + count).getNumberValue().intValue());
                    break;
                case 'e':
                case 'E':
                case 'f':
                case 'g':
                case 'G':
                case 'a':
                case 'A':
                    list.add(numberList.get(j + count).getNumberValue().floatValue());
                    break;
                case 'c':
                case 'C':
                    list.add(numberList.get(j + count).getStringValue().charAt(0));
                    break;
                case 's':
                case 'S':
                    list.add(numberList.get(j + count).getStringValue());
                    break;
                case 'b':
                case 'B':
                    list.add(numberList.get(j + count).getBooleanValue());
                    break;
                case '%':
                    count--;
                    break;
                case 't':
                case 'T':
                    throw new OperationFailedException("format a date/time", "Date/Time conversions are not supported.", "Use Date package for formatting the date/time.");
                default:
                    throw new InvalidUserInputException("Unexpected conversion type.", typeString, "A supported conversion type.", EXPLANATION);
            }
        }
        return list;
    }
}
