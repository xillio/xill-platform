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
import com.google.inject.Singleton;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.stream.utils.StreamUtils;
import nl.xillio.xill.services.files.TextFileReader;

import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * Read text from a plain text file using the given encoding.
 * @deprecated use the Stream package to read/write files.
 * @author Thomas biesaart
 */
@Singleton
@Deprecated
public class GetTextConstruct extends Construct {
    private final TextFileReader textFileReader;

    @Inject
    GetTextConstruct(TextFileReader textFileReader) {
        this.textFileReader = textFileReader;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (file, encoding) -> process(context, file, encoding),
                new Argument("input", ATOMIC),
                new Argument("encoding", NULL, ATOMIC));
    }

    private MetaExpression process(final ConstructContext context, final MetaExpression source, final MetaExpression encoding) {
        // Get the charset and path.
        Charset charset = StreamUtils.getCharset(encoding);
        Path path = getPath(context, source);

        return fromValue(textFileReader.getText(path, charset));
    }
}
