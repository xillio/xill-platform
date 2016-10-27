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

import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.List;

/**
 * This is the main implementation of the {@link StringUtilityService}
 */
@Singleton
public class StringUtilityServiceImpl implements StringUtilityService {

    @Override
    public boolean contains(final String parent, final String child) {
        return parent.contains(child);
    }

    @Override
    public boolean endsWith(final String first, final String second) {
        return first.endsWith(second);
    }

    @Override
    public String format(final String text, final List<Object> args) {
        return String.format(text, args.toArray());
    }

    @Override
    public int indexOf(final String haystack, final String needle, final int index) {
        return haystack.indexOf(needle, index);
    }

    @Override
    public String join(final String[] input, final String delimiter) {
        return StringUtils.join(input, delimiter);
    }

    @Override
    public String repeat(final String value, final int repeat) {
        return StringUtils.repeat(value, repeat);
    }

    @Override
    public String replaceAll(final String haystack, final String needle, final String replacement) {
        return haystack.replace(needle, replacement);
    }

    @Override
    public String replaceFirst(final String haystack, final String needle, final String replacement) {
        return StringUtils.replaceOnce(haystack, needle, replacement);
    }

    @Override
    public String[] split(final String haystack, final String needle) {
        return haystack.split(needle, -1);
    }

    @Override
    public boolean startsWith(final String haystack, final String needle) {
        return haystack.startsWith(needle);
    }

    @Override
    public String subString(final String text, final int start, final int end) {
        return text.substring(start, end);
    }

    @Override
    public String toLowerCase(final String toLower) {
        return toLower.toLowerCase();
    }

    @Override
    public String toUpperCase(final String toUpper) {
        return toUpper.toUpperCase();
    }

    @Override
    public String trim(final String toTrim) {
        // Ensure unicode non breaking space is converted also
        return replaceAll(toTrim, "\u00A0", " ").trim();
    }

    @Override
    public String trimInternal(final String toTrim) {
        return StringUtils.replacePattern(trim(toTrim), "\\s+", " ");
    }

    @Override
    public String wrap(final String text, final int width, final boolean wrapLongWords) {
        return WordUtils.wrap(text, width, "\n", wrapLongWords);
    }
}
