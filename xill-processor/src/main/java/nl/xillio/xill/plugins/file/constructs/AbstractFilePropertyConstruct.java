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
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class represents an abstract implementation of a construct that will check a property of a file, e.g. canWrite, creationDate.
 * NOTE: The input file will be checked for existence. If the file does not exist, this construct will fail
 *
 * @param <T> the type of property to extract
 * @author Thomas biesaart
 */
abstract class AbstractFilePropertyConstruct<T> extends Construct {
    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                path -> process(context, path),
                new Argument("path", ATOMIC)
        );
    }

    private MetaExpression process(ConstructContext context, MetaExpression path) {
        Path file = getPath(context, path);

        // Read javadoc, this is not the same as Files.notExists
        if (!Files.exists(file)) {
            throw new RobotRuntimeException(file + " does not exist");
        }

        try {
            return parse(process(file));
        } catch (AccessDeniedException e) {
            throw new RobotRuntimeException("Access denied to " + e.getFile(), e);
        } catch (IOException e) {
            throw new OperationFailedException("read " + file.toFile().getAbsolutePath(), e.getMessage(), e);
        }
    }

    /**
     * Check the concrete property for the passed path.
     * This path has been checked for existence so no need to check that.
     *
     * @param path the path
     * @return true if the passed files holds the concrete property
     * @throws IOException if the path could not be read
     */
    protected abstract T process(Path path) throws IOException;

    /**
     * Parse a result from {@link #process(Path)} to a {@link MetaExpression}.
     *
     * @param input the input
     * @return the expression
     */
    protected abstract MetaExpression parse(T input);
}
