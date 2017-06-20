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
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.io.IOStream;
import nl.xillio.xill.api.io.SimpleIOStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * This construct will open a file or resource handle with read permissions.
 *
 * @author Thomas biesaart
 */
public class OpenReadConstruct extends AbstractOpenConstruct {

    @Override
    protected IOStream open(Path path) throws IOException {
        return fileStreamFactory.openRead(path);
    }

    @Override
    protected IOStream openSystemStream(MetaExpression pathVar, ConstructContext context) {
        String path = pathVar.getStringValue();
        try {
            InputStream stream = context.getResourceLoader().getResourceAsStream(path);

            if (stream == null) {
                return null;
            }

            return new SimpleIOStream(stream, "Resource: " + path);

        } catch (IOException e) {
            throw new OperationFailedException(
                    "open a resource",
                    e.getMessage(),
                    "Make sure the resource exists",
                    e
            );
        }
    }
}
