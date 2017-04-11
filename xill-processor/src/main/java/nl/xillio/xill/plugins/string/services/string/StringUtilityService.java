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
import nl.xillio.xill.plugins.string.StringXillPlugin;

import java.util.List;

/**
 * This interface represents some of the operations for the {@link StringXillPlugin}.
 */
@ImplementedBy(StringUtilityServiceImpl.class)
public interface StringUtilityService {

    /**
     * Checks whether the child string is contained in the parent string.
     *
     * @param parent The parent string.
     * @param child  The child string.
     * @return Returns true if the child is contained in the parent.
     */
    boolean contains(String parent, String child);

    /**
     * Checks if the first string ends with the second.
     *
     * @param haystack The string we're checking.
     * @param needle   The string we're searching.
     * @return A boolean wheter the haystack string ends with the needle.
     */
    boolean endsWith(String haystack, String needle);

    /**
     * Formats a texts with specified arguments.
     *
     * @param text The text we format.
     * @param args The arguments we give.
     * @return Returns the formatted text.
     */
    String format(String text, List<Object> args);

    /**
     * Returns the index of the first occurrence of the needle in the haystack, starting from an index.
     *
     * @param haystack The haystack we're searching through.
     * @param needle   The needle we're searching.
     * @param index    The index from which we start searching.
     * @return The index of the first occurrence of the needle higher than the given index.
     */
    int indexOf(String haystack, String needle, int index);

    /**
     * Returns the index of the last occurrence of the needle in the haystack.
     * @param haystack  The haystack we are searching in.
     * @param needle    The needle we are looking for.
     * @return The index of the last occurrence of the needle in the haystack.
     */
    int lastIndexOf(String haystack, String needle);

    /**
     * Joins an array of strings by a delimiter.
     *
     * @param input     The array of strings that need joining.
     * @param delimiter The delimiter.
     * @return A string which is the join of the input.
     */
    String join(String[] input, String delimiter);

    /**
     * Recieves a string and returns repeated a few times.
     *
     * @param value  The string we're repeating.
     * @param repeat An int indicating how many times we're repeating.
     * @return A string with the value repeated.
     */
    String repeat(String value, int repeat);

    /**
     * Replaces each substring of the needle in the haystack with the replacement string.
     *
     * @param haystack    The string we're altering.
     * @param needle      The substrings we're replacing.
     * @param replacement The replacement string.
     * @return The haystack string with all the needles replaced.
     */
    String replaceAll(String haystack, String needle, String replacement);

    /**
     * Replaces the first substring of the needle in the haystack with the replacement string.
     *
     * @param haystack    The string we're altering.
     * @param needle      The substrings we're replacing.
     * @param replacement The replacement string.
     * @return The haystack string with the first needle replaced.
     */
    String replaceFirst(String haystack, String needle, String replacement);

    /**
     * Recieves a haystack string and splits it in the needle string.
     *
     * @param haystack The haystack string.
     * @param needle   The needle string.
     * @return An array of substrings.
     */
    String[] split(String haystack, String needle);

    /**
     * Recieves a haystack string and returns wheter it starts with the given needle string.
     *
     * @param haystack The string we're checking.
     * @param needle   The string we're searching.
     * @return Returns wheter the haystack string starts with the needle.
     */
    boolean startsWith(String haystack, String needle);

    /**
     * Returns a substring of a given string at the indices of start and end.
     *
     * @param text  The main string.
     * @param start The start index.
     * @param end   The end index.
     * @return The substring.
     */
    String subString(String text, int start, int end);

    /**
     * Recieves a string and returns in lowercased.
     *
     * @param toLower The string to lower.
     * @return The provided string lowercased.
     */
    String toLowerCase(String toLower);

    /**
     * Recieves a string and returns it uppercased.
     *
     * @param toUpper The string to upper.
     * @return The provided string uppercased.
     */
    String toUpperCase(String toUpper);

    /**
     * Recieves a string and returns a trimmed version.
     *
     * @param toTrim the string to trim.
     * @return the trimmed version of the string.
     */
    String trim(String toTrim);

    /**
     * Trim a string and replace all in between whitespace groups by single spaces.
     *
     * @param toTrim the string to trim
     * @return the trimmed version of the string
     */
    String trimInternal(final String toTrim);

    /**
     * wraps a single line of text, identifying words by ' '
     *
     * @param text          The text we're wrapping, may be null.
     * @param width         The column to wrap the words at, less than 1 is treated as 1.
     * @param wrapLongWords True if long words (such as URLs) should be wrapped.
     * @return Returns the wrapped text.
     */
    String wrap(String text, int width, boolean wrapLongWords);

}
