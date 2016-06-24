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

/**
 * This interface represents an object that can calculate the size of a file or folder.
 *
 * @author Thomas Biesaart
 */
@ImplementedBy(FileUtilitiesImpl.class)
public interface FileSizeCalculator {
    /**
     * This method will get the size of a file or folder.
     * In the case of a folder it will iterate through all the files in the folder and return the size.
     *
     * @param path the path
     * @return the size
     * @throws IOException if calculating the file size fails
     */
    long getSize(Path path) throws IOException;
}
