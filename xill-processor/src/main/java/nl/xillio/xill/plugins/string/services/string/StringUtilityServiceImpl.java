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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.IllegalFormatException;
import java.util.List;

/**
 * This is the main implementation of the {@link StringUtilityService}
 */
@Singleton
public class StringUtilityServiceImpl implements StringUtilityService {

    @Override
    public int damlev(final String source, final String target) {
        int[] workspace = new int[1024];
        int lenS = source.length();
        int lenT = target.length();

        if (lenS * lenT > workspace.length) {
            workspace = new int[(source.length() + 1) * (target.length() + 1)];
        }

        int lenS1 = lenS + 1;
        int lenT1 = lenT + 1;

        if (lenT1 == 1) {
            return lenS1 - 1;
        }
        if (lenS1 == 1) {
            return lenT1 - 1;
        }

        int[] dl = workspace;
        int dlIndex = 0;
        int sPrevIndex = 0, tPrevIndex = 0, rowBefore, min, cost, tmp;
        int tri = lenS1 + 2;

        // start row with constant
        for (tmp = 0; tmp < lenT1; tmp++) {
            dl[dlIndex] = tmp;
            dlIndex += lenS1;
        }
        for (int sIndex = 0; sIndex < lenS; sIndex++) {
            dlIndex = sIndex + 1;
            dl[dlIndex] = dlIndex; // start column with constant
            for (int tIndex = 0; tIndex < lenT; tIndex++) {
                rowBefore = dlIndex;
                dlIndex += lenS1;
                // deletion
                min = dl[rowBefore] + 1;
                // insertion
                tmp = dl[dlIndex - 1] + 1;
                if (tmp < min) {
                    min = tmp;
                }
                cost = 1;
                if (source.charAt(sIndex) == target.charAt(tIndex)) {
                    cost = 0;
                }
                if (sIndex > 0 && tIndex > 0) {
                    if (source.charAt(sIndex) == target.charAt(tPrevIndex) && source.charAt(sPrevIndex) == target.charAt(tIndex)) {
                        tmp = dl[rowBefore - tri] + cost;
                        // transposition
                        if (tmp < min) {
                            min = tmp;
                        }
                    }
                }
                // substitution
                tmp = dl[rowBefore - 1] + cost;
                if (tmp < min) {
                    min = tmp;
                }
                dl[dlIndex] = min;
                tPrevIndex = tIndex;
            }
            sPrevIndex = sIndex;
        }
        return dl[dlIndex];
    }

    @Override
    public boolean contains(final String parent, final String child) {
        return parent.contains(child);
    }

    @Override
    public boolean endsWith(final String first, final String second) {
        return first.endsWith(second);
    }

    @Override
    public String format(final String text, final List<Object> args) throws IllegalFormatException {
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
