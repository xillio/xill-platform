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
package nl.xillio.xill.plugins.web.services.web;

import com.google.inject.ImplementedBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Provides an interface for file services.
 *
 * @author Ivor van der Hoog
 * @author Thomas Biesaart
 */
@ImplementedBy(FileServiceImpl.class)
public interface FileService {

    /**
     * Copies a source File to a destination File.
     *
     * @param sourceFile      The path to the source File.
     * @param destinationFile The path to the destination File.
     * @throws IOException when copying the file failed
     */
    void copyFile(Path sourceFile, Path destinationFile) throws IOException;

    /**
     * Returns the absolute pathname as a String.
     *
     * @param file The file we want the absolute path from.
     * @return The absolute path of the file.
     */
    String getAbsolutePath(File file);

    /**
     * Creates a temporary {@link File} with a given prefix and suffix.
     *
     * @param prefix The prefix.
     * @param suffix The suffix.
     * @return Returns a temporary file.
     * @throws IOException when an error occurs
     */
    File createTempFile(String prefix, String suffix) throws IOException;

    /**
     * Writes a string to a {@link File}.
     *
     * @param filePath The file we want to write to.
     * @param text     The text we're writing.
     * @throws IOException then a writing error occurs
     */
    void writeStringToFile(final Path filePath, final String text) throws IOException;

}
