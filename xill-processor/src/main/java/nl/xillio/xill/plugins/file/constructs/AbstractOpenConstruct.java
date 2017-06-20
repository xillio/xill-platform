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
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.io.IOStream;
import nl.xillio.xill.plugins.file.services.files.FileStreamFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This construct will open a stream to read.
 *
 * @author Thomas biesaart
 */
abstract class AbstractOpenConstruct extends Construct {
    protected FileStreamFactory fileStreamFactory;

    @Inject
    void setFileStreamFactory(FileStreamFactory fileStreamFactory) {
        this.fileStreamFactory = fileStreamFactory;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                path -> process(path, context),
                new Argument("path")
        );
    }

    MetaExpression process(MetaExpression pathVar, ConstructContext context) {
        IOStream stream = openSystemStream(pathVar, context);

        if (stream == null) {
            Path path = getPath(context, pathVar);
            stream = tryOpen(path);
        }

        return fromValue(stream);
    }

    /**
     * Get a system stream to the resource. The default implementation is a NO-OP.
     * @param pathVar the path variable
     * @param context the execution context
     * @return a stream if available, otherwise null
     */
    protected IOStream openSystemStream(MetaExpression pathVar, ConstructContext context) {
        return null;
    }

    protected IOStream tryOpen(Path path) {
        try {
            return open(path);
        } catch (IOException e) {
            throw new OperationFailedException(" open stream to " + path, e.getMessage(), e);
        }
    }

    protected abstract IOStream open(Path path) throws IOException;


}
