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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * Formats the string with the provided values.
 * Does not support Time/Date.
 *
 * @author Sander
 */
public class FormatConstruct extends Construct {

    private static final String EXPLANATION = "use String;\nString.format(\"%3$2s %1$2s %1$2s %2$2s\" , [\"a\", \"b\", \"c\"] );";
    private static final Set<Character> INT_CHARACTERS = new HashSet<>();
    private static final Set<Character> FLOAT_CHARACTERS = new HashSet<>();
    private final RegexService regexService;
    private final StringUtilityService stringService;

    static {
        INT_CHARACTERS.addAll(Arrays.asList('d', 'o', 'x', 'X', 'h', 'H'));
        FLOAT_CHARACTERS.addAll(Arrays.asList('e', 'E', 'f', 'g', 'G', 'a', 'A'));
    }

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
        List<MetaExpression> numberList = valueVar.getValue();
        List<Object> list = new ArrayList<>();
        int count = 0;
        String typeString;
        for (int j = 0; j < numberList.size() - count; j++) {
            if (j >= formatList.size()) {
                break;
            }
            typeString = formatList.get(j).getStringValue();
            char current = typeString.charAt(typeString.length() - 1);

            Object newObject = parseCharacter(current, numberList.get(j + count));
            if (newObject != null) {
                list.add(newObject);
            } else if ("%".equals(String.valueOf(current))) {
                count--;
            } else
                throw new InvalidUserInputException("Unexpected conversion type.", typeString, "A supported conversion type.", EXPLANATION);
        }
        return list;
    }

    private Object parseCharacter(char current, MetaExpression expression) {
        Object returnValue = null;
        if (INT_CHARACTERS.contains(current)) {
            returnValue = expression.getNumberValue().intValue();
        } else if (FLOAT_CHARACTERS.contains(current)) {
            returnValue = expression.getNumberValue().floatValue();
        } else if ("c".equalsIgnoreCase(String.valueOf(current))) {
            returnValue = expression.getStringValue().charAt(0);
        } else if ("s".equalsIgnoreCase(String.valueOf(current))) {
            returnValue = expression.getStringValue();
        } else if ("b".equalsIgnoreCase((String.valueOf(current)))) {
            returnValue = expression.getBooleanValue();
        } else if ("t".equalsIgnoreCase(String.valueOf(current))) {
            throw new OperationFailedException("format a date/time", "Date/Time conversions are not supported.", "Use Date package for formatting the date/time.");
        }
        return returnValue;
    }
}
