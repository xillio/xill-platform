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

import me.biesaart.utils.IOUtils;
import me.biesaart.utils.StringUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;


public class FileUtilitiesImplTest {

    private final FileUtilitiesImpl fileUtilities = new FileUtilitiesImpl();

    @Test
    public void testCopy() throws Exception {
        Path sourceFolder = Files.createTempDirectory(getClass().getSimpleName());
        Path targetFolder = Files.createTempDirectory(getClass().getSimpleName() + "-copy");

        for (int i = 0; i < 5; i++) {
            Path file = sourceFolder.resolve(i + ".txt");
            Files.createFile(file);
        }

        fileUtilities.copy(sourceFolder, targetFolder);

        // Read all the files
        List<Path> filesInTarget = new ArrayList<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(targetFolder)) {
            for (Path path : directoryStream) {
                filesInTarget.add(path);
            }
        }

        assertEquals(filesInTarget.size(), 5);


        // Now delete the source and target
        fileUtilities.delete(sourceFolder);
        fileUtilities.delete(targetFolder);
    }

    @Test(expectedExceptions = NoSuchFileException.class)
    public void testCopyNoExist() throws IOException {
        Path folder = Paths.get("NO EXIST");

        fileUtilities.copy(folder, folder);
    }

    @Test
    public void testGetSize() throws IOException {
        // Create file
        Path file = Files.createTempFile(getClass().getSimpleName(), ".txt");
        Files.copy(IOUtils.toInputStream("Hello World"), file, StandardCopyOption.REPLACE_EXISTING);

        long size = fileUtilities.getSize(file);

        assertEquals(size, 11);

        Files.delete(file);
    }

    @Test
    public void testGetSizeDir() throws IOException {
        Path folder = Files.createTempDirectory(getClass().getSimpleName());

        for (int i = 0; i < 10; i++) {
            Path file = folder.resolve(i + ".txt");
            Files.copy(IOUtils.toInputStream(StringUtils.repeat("Hello ", i)), file);
        }

        long size = fileUtilities.getSize(folder);

        assertEquals(size, 270);
        fileUtilities.delete(folder);
    }
}