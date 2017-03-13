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
package nl.xillio.xill.plugins.file.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.services.resourceLibraries.ContentTypeLibrary;

import java.util.Optional;

/**
 *  Construct for extracting mime type from a file name. It does not check if a file exists.
 *
 *  @author Paul van der Zandt
 */
public class GetMimeTypeConstruct extends Construct {

    private final ContentTypeLibrary contentTypeLibrary;

    @Inject
    public GetMimeTypeConstruct(ContentTypeLibrary contentTypeLibrary) {
        this.contentTypeLibrary = contentTypeLibrary;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                path -> process(path, context),
                new Argument("path", ATOMIC)
        );
    }

    private MetaExpression process(MetaExpression path, ConstructContext context) {
        String filename = path.getStringValue();
        return fromValue(getExtension(filename).orElse(null));
    }

    private Optional<String> getExtension(String filename) {
        int index = filename.lastIndexOf('.');
        if (index == -1) {
            return Optional.empty();
        }

        return contentTypeLibrary.get(filename.substring(index).toLowerCase());
    }

}
