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

import nl.xillio.xill.docgen.exceptions.ParsingException;

/**
 * This interface represents an object that can generate output from a {@link DocumentationEntity}
 *
 * @author Thomas Biesaart
 * @author Ivor van der Hoog
 * @since 12-8-2015
 */
public interface DocumentationGenerator extends AutoCloseable {
    /**
     * Generate a documentation file from a {@link DocumentationEntity}
     *
     * @param entity the {@link DocumentationEntity} to generate the output for
     * @throws ParsingException      if parsing the entity failed
     * @throws IllegalStateException if this DocumentationGenerator has already been closed
     */
    void generate(DocumentationEntity entity) throws ParsingException;

    /**
     * Generate the index of all packages
     *
     * @throws ParsingException if parsing wasn't successful
     */
    void generateIndex() throws ParsingException;

    void setProperty(String name, String value);

    /**
     * Get the name of this generator.
     *
     * @return the name
     */
    String getIdentity();
}
