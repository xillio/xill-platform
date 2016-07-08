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
package nl.xillio.exiftool.query;

/**
 * This interface represents an object that can convert a tag name to a naming convention.
 *
 * @author Thomas Biesaart
 */
public interface TagNameConvention {
    /**
     * Convert a name to this convention.
     *
     * @param originalTagName the original tag name
     * @return the convention name
     */
    String toConvention(String originalTagName);
}
