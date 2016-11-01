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


import java.util.Comparator;
import java.util.List;

/**
 * This interface represents a piece of documentation of xill constructs.
 *
 * @author Thomas Biesaart
 * @author Ivor van der Hoog
 * @since 12-8-2015
 */
public interface DocumentationEntity extends PropertiesProvider {
    Comparator<DocumentationEntity> SORT_BY_IDENTITY = new IdentitySorter();

    /**
     * Get the identity of this name.
     *
     * @return a single word identity that represents this entry. This would generally be a document name.
     */
    String getIdentity();

    /**
     * Get the type of entity.
     *
     * @return a single word identifier that represents the entity type
     */
    String getType();

    /**
     * Get a list of tags for this entity.
     *
     * @return the tags
     */
    List<String> getTags();

    class IdentitySorter implements Comparator<DocumentationEntity> {

        @Override
        public int compare(DocumentationEntity o1, DocumentationEntity o2) {
            return o1.getIdentity().compareTo(o2.getIdentity());
        }
    }
}
