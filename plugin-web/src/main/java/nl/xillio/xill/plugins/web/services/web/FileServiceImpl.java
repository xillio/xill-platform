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

import com.google.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * The implementation of the {@link FileService} interface.
 */
@Singleton
public class FileServiceImpl implements FileService {

    @Override
    public void copyFile(final Path sourceFile, final Path destinationFile) throws IOException {
        Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public String getAbsolutePath(final File file) {
        return file.getAbsolutePath();
    }

    @Override
    public File createTempFile(final String prefix, final String suffix) throws IOException {
        File file = File.createTempFile(prefix, suffix);
        file.deleteOnExit();
        return file;
    }

    @Override
    public void writeStringToFile(final Path filePath, final String text) throws IOException {
        Files.write(filePath, text.getBytes());
    }

}
