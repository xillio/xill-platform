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
import nl.xillio.xill.plugins.file.services.FileSystemIterator;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * This class provides a base for constructs that provide an iterator.
 *
 * @author Thomas Biesaart
 * @see IterateFoldersConstruct
 * @see IterateFilesConstruct
 */
abstract class AbstractIteratorConstruct extends Construct {

    private FileSystemIterator fileIterator;

    @Inject
    void setFileIterator(FileSystemIterator iterator) {
        this.fileIterator = iterator;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (uri, recursive) -> process(context, uri, recursive),
                new Argument("uri", ATOMIC),
                new Argument("recursive", FALSE, ATOMIC));
    }


    private MetaExpression process(ConstructContext context, MetaExpression uri, MetaExpression recursive) {
        Path file = getPath(context, uri);
        boolean isRecursive = recursive.getBooleanValue();

        return tryBuildIterator(file, isRecursive);
    }

    private MetaExpression tryBuildIterator(Path file, boolean isRecursive) {
        try {
            return buildIterator(file, isRecursive);
        } catch (FileSystemException e) {
            throw new OperationFailedException("read " + e.getFile(), e.getMessage(), e);
        } catch (IOException e) {
            throw new OperationFailedException("iterate " + file, e.getMessage(), e);
        }
    }


    protected abstract MetaExpression buildIterator(Path file, boolean isRecursive) throws IOException;


    protected Iterator<Path> iterateFiles(Path folder, boolean recursive) throws IOException {
        return fileIterator.iterateFiles(folder, recursive);
    }

    protected Iterator<Path> iterateFolders(Path folder, boolean recursive) throws IOException {
        return fileIterator.iterateFolders(folder, recursive);
    }
}
