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
package nl.xillio.xill.plugins.file.services;

import com.google.inject.ImplementedBy;
import nl.xillio.xill.plugins.file.services.files.FileUtilitiesImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * This interface represents an object that is capable of providing iterators for files.
 *
 * @author Thomas biesaart
 */
@ImplementedBy(FileUtilitiesImpl.class)
public interface FileSystemIterator {

    /**
     * Create an Iterator that will cover only files in a specific directory
     *
     * @param folder    the folder to list files from
     * @param recursive if this is set to true the iterator will also contain all files in all subdirectories
     * @return the iterator
     * @throws IOException when the folder does not exist or is not a folder at all
     */
    Iterator<Path> iterateFiles(Path folder, boolean recursive) throws IOException;

    /**
     * Create an Iterator that will cover only folders in a specific directory
     *
     * @param folder    the folder to list files from
     * @param recursive if this is set to true the iterator will also contain all files in all subdirectories
     * @return the iterator
     * @throws IOException when the folder does not exist or is not a folder at all
     */
    Iterator<Path> iterateFolders(Path folder, boolean recursive) throws IOException;
}
