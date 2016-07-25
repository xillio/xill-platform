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
package nl.xillio.xill.plugins.file.services.files;

import com.google.inject.ImplementedBy;
import nl.xillio.xill.services.XillService;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This {@link XillService} is responsible for various file operations
 */
@ImplementedBy(FileUtilitiesImpl.class)
public interface FileUtilities extends XillService {

    /**
     * Copy a source file to a target destination, overwriting it if it exists
     *
     * @param source the source file
     * @param target the target file
     * @throws IOException when the operation failed
     */
    void copy(Path source, Path target) throws IOException;

    /**
     * Create a folder at the specific location if it does not exist
     *
     * @param folder the folder
     * @throws IOException when the operation failed
     */
    void createFolder(Path folder) throws IOException;

    /**
     * Returns true if the file exists
     *
     * @param file the file to check
     * @return true if and only if the file exists
     */
    boolean exists(Path file);

    /**
     * Delete a file or folder
     *
     * @param file the file
     * @throws IOException when the operation failed
     */
    void delete(Path file) throws IOException;

    /**
     * Move a source file to a target path
     *
     * @param source the source file
     * @param target the target file
     * @param replaceExisting it will replace existing target file when set to true, it will fail if target file already exists when set to false
     * @throws IOException when operation failed
     */
    void move(Path source, Path target, boolean replaceExisting) throws IOException;
}
