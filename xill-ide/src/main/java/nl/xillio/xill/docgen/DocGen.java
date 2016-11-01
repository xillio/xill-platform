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

import com.google.inject.ImplementedBy;
import nl.xillio.xill.docgen.exceptions.ParsingException;
import nl.xillio.xill.docgen.impl.XillDocGen;

import java.io.File;

/**
 * This interface represents the main entry point of the documentation generation system
 *
 * @author Thomas Biesaart
 * @author Ivor van der Hoog
 * @since 12-8-2015
 */
@ImplementedBy(XillDocGen.class)
public interface DocGen {
    /**
     * Create a {@link DocumentationParser}
     *
     * @return the parser
     */
    DocumentationParser getParser();


    /**
     * Create a {@link DocumentationSearcher}
     *
     * @return the searcher
     */
    DocumentationSearcher getSearcher();

    /**
     * Get the configuration that was used to initialize this {@link DocGen}
     *
     * @return the configuration
     */
    DocGenConfiguration getConfig();

    /**
     * Generate the global index
     *
     * @throws ParsingException if generating the index fails
     */
    void generateIndex() throws ParsingException;

    /**
     * Create a {@link DocumentationGenerator}
     *
     * @param collectionIdentity The name of this generator.
     * @param rootFolder         The root folder where the generator will place the generated files
     * @return the generator
     */
    DocumentationGenerator getGenerator(String collectionIdentity, File rootFolder);
}
