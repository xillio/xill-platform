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
package nl.xillio.xill.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Settings needed by the Ace editor Xill mode
 *
 * @author Geert Konijnendijk
 */
public class HighlightSettings {

    private List<String> keywords = new ArrayList<>();
    private List<String> builtins = new ArrayList<>();

    /**
     * Add a single Xill keyword to the settings
     *
     * @param keyword A Xill keyword
     */
    public void addKeyword(String keyword) {
        keywords.add(keyword);
    }

    /**
     * Add a single Xill builtin to the settings
     *
     * @param builtin A Xill builtin
     */
    public void addBuiltin(String builtin) {
        builtins.add(builtin);
    }

    /**
     * Add multiple keywords to the settings
     *
     * @param keywords Xill keywords
     */
    public void addKeywords(String... keywords) {
        addKeywords(Arrays.asList(keywords));
    }

    /**
     * Add multiple builtins to the settings
     *
     * @param builtins Xill builtins
     */
    public void addBuiltins(String... builtins) {
        addBuiltins(Arrays.asList(builtins));
    }

    /**
     * Add multiple keywords to the settings
     *
     * @param keywords Xill keywords
     */
    public void addKeywords(Collection<String> keywords) {
        this.keywords.addAll(keywords);
    }

    /**
     * Add multiple builtins to the settings
     *
     * @param builtins Xill builtins
     */
    public void addBuiltins(Collection<String> builtins) {
        this.builtins.addAll(builtins);
    }

    /**
     * @return All keywords separated with "|"
     */
    public String getKeywords() {
        return StringUtils.join(keywords, "|");
    }

    /**
     * @return All builtins separated with "|"
     */
    public String getBuiltins() {
        return StringUtils.join(builtins, "|");
    }
}
