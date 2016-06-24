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
package nl.xillio.xill.api.preview;

/**
 * An object which content can support search and replace operations.
 */
public interface Replaceable extends Searchable {

    /**
     * Replaces all occurrences of the search expression with the given replacement.
     *
     * @param replacement the replacement string
     */
    public void replaceAll(String replacement);

    /**
     * Replace a given occurrence of a search expression with the given replacement.
     *
     * @param occurrence  the occurrence to be replaced
     * @param replacement the replacement string
     */
    public void replaceOne(int occurrence, String replacement);

}
