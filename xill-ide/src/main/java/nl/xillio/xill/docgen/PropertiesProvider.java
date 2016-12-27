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
package nl.xillio.xill.docgen;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This interface represents an object that can hold properties
 *
 * @author Thomas Biesaart
 * @author Ivor van der Hoog
 * @since 12-8-2015
 */
@FunctionalInterface
public interface PropertiesProvider {
    Map<String, Object> getProperties();

    /**
     * Extract the properties from a collection of PropertiesProviders
     *
     * @param collection the properties collection
     * @return a list of sets of properties
     */
    static List<Map<String, Object>> extractContent(Collection<? extends PropertiesProvider> collection) {
        return collection.stream()
                .map(PropertiesProvider::getProperties)
                .collect(Collectors.toList());
    }
}
