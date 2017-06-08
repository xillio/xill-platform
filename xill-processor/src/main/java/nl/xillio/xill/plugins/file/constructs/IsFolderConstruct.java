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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tests whether the file denoted by this abstract pathname is a folder.
 *
 * @author Paul van der Zandt, Xillio
 * @author Thomas biesaart
 */
public class IsFolderConstruct extends Construct {
    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                path -> process(context, path),
                new Argument("path", ATOMIC));
    }

    static MetaExpression process(final ConstructContext context, final MetaExpression path) {
        Path file = getPath(context, path);

        return fromValue(Files.isDirectory(file));

    }

}
