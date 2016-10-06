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
package nl.xillio.xill.plugins.string.services.string;

import com.google.inject.ImplementedBy;
import nl.xillio.xill.plugins.string.exceptions.FailedToGetMatcherException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * This interface represents a collection of operations using a shared regex Matcher
 */
@ImplementedBy(RegexServiceImpl.class)
public interface RegexService {

    /**
     * @param regex   the pattern
     * @param value   the haystack
     * @param timeout in milliseconds
     * @return the matcher
     * @throws FailedToGetMatcherException Is thrown when the matcher fails.
     * @throws IllegalArgumentException    Is thrown when an illegal argument is passed to the regular expression.
     * @throws PatternSyntaxException      Is thrown when the regular expression has a syntax error.
     */
    Matcher getMatcher(String regex, String value, int timeout) throws FailedToGetMatcherException, IllegalArgumentException;


    /**
     * Returns the default Timeout time.
     *
     * @return default Timeout time.
     */
    int getRegexTimeout();

    /**
     * Attempts to match an entire region to a pattern.
     *
     * @param matcher The Matcher we're matching with.
     * @return Returns wheter the matching succeeded.
     */
    boolean matches(Matcher matcher);

    /**
     * Lets the matcher replace all subsequences of a given input with the replacement.
     *
     * @param matcher     The matcher we're using.
     * @param replacement The replacement string.
     * @return Returns a modified string.
     */
    String replaceAll(Matcher matcher, String replacement);

    /**
     * Lets the matcher replace the first subsequence of a given input with the replacement.
     *
     * @param matcher     The matcher we're using.
     * @param replacement The replacement string.
     * @return Returns a modified string.
     */
    String replaceFirst(Matcher matcher, String replacement);

    /**
     * Returns a list of matches form a matcher.
     *
     * @param matcher The Matcher we're using.
     * @return Returns a list of strings with matches.
     */
    List<String> tryMatch(Matcher matcher);

    /**
     * Returns a list of matches from a matcher
     *
     * @param matcher The Matcher we're using.
     * @return Returns a list of strings with matches or null.
     */
    List<String> tryMatchElseNull(Matcher matcher);

    /**
     * Escape the input string so it can be included in a regex as a literal
     *
     * @param toEscape The string to escape
     * @return A string that can be included in a regex as a literal
     */
    String escapeRegex(String toEscape);

}
