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

/**
 * This interface represents an object that can run search queries on a collection of entities
 *
 * @author Thomas Biesaart
 * @author Ivor van der Hoog
 * @since 12-8-2015
 */
public interface DocumentationSearcher {
    String[] search(String query);

    void index(String packet, DocumentationEntity entity);
}
