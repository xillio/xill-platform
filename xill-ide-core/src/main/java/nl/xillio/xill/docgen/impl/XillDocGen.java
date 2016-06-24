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
package nl.xillio.xill.docgen.impl;

import freemarker.template.Configuration;
import nl.xillio.xill.docgen.*;
import nl.xillio.xill.docgen.exceptions.ParsingException;

import java.io.File;


/**
 * This {@link DocGen} uses a template system and a elasticsearch backed searcher
 *
 * @author Thomas Biesaart
 * @author Ivor van der Hoog
 * @since 12-8-2015
 */
public class XillDocGen implements DocGen {
    private final DocGenConfiguration config = new DocGenConfiguration();
    private DocumentationGenerator lastGenerator;

    @Override
    public DocumentationParser getParser() {
        return new XmlDocumentationParser();
    }

    @Override
    public DocumentationGenerator getGenerator(String collectionIdentity, File rootFolder) {
        lastGenerator = new FreeMarkerDocumentationGenerator(
                collectionIdentity,
                getFreeMarkerConfig(),
                rootFolder);
        return lastGenerator;
    }

    @Override
    public DocumentationSearcher getSearcher() {
        return new InMemoryDocumentationSearcher();
    }

    @Override
    public DocGenConfiguration getConfig() {
        return config;
    }

    @Override
    public void generateIndex() throws ParsingException {
        if (lastGenerator == null) {
            throw new ParsingException("No packages have been parsed");
        }

        lastGenerator.generateIndex();
    }

    Configuration getFreeMarkerConfig() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setClassForTemplateLoading(getClass(), config.getTemplateUrl());

        return configuration;
    }


}
