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
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.file.services.files.FileUtilities;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.LinkedHashMap;

/**
 * This construct will copy a file or folder to a target destination.
 */
@Singleton
public class CopyConstruct extends Construct {
    @Inject
    private FileUtilities fileUtils;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (source, target) -> process(context, fileUtils, source, target),
                new Argument("source", ATOMIC),
                new Argument("target", ATOMIC));
    }
    static MetaExpression process(final ConstructContext context, final FileUtilities fileUtils, final MetaExpression source, final MetaExpression target) {

        Path sourceFile = getPath(context, source);
        Path targetFile = getPath(context, target);
        try {
            fileUtils.copy(sourceFile, targetFile);
        } catch (NoSuchFileException e) {
            if (e.getFile().equals(sourceFile.toFile().getPath())) {
                throw new InvalidUserInputException("Source file does not exist.", sourceFile.toFile().getPath(), "An existing file.", "use System, File;\n" +
                        "var result = File.copy(\"file.txt\", \"file_copy.txt\");\n" +
                        "System.print(result);", e);
            } else {
                throw new RobotRuntimeException(e.getFile() + " does not exist", e);
            }
        } catch (IOException e) {
            throw new OperationFailedException("copy " + sourceFile + " to " + targetFile, e.getMessage(), "Check if target path can be created.", e);
        }

        //Build the result
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();
        result.put("from", fromValue(sourceFile.toString()));
        result.put("into", fromValue(targetFile.toString()));
        return fromValue(result);
    }
}
