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
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.errors.OperationFailedException;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.*;

import static org.testng.Assert.assertEquals;

public class SimpleTextFileReaderTest extends TestUtils {
    private SimpleTextFileReader reader = new SimpleTextFileReader();
    // Temporary file used in the tests.
    private Path tempFile;

    @BeforeTest
    public void createTempFile() throws IOException {
        tempFile = Files.createTempFile(getClass().getSimpleName(), ".txt");
    }

    @AfterTest
    public void cleanup() throws IOException {
        Files.delete(tempFile);
    }

    /**
     * Test the process under normal circumstances.
     */
    @Test
    public void testProcessNormal() throws IOException {
        // Create test file.
        String content = "Get text test";
        Files.copy(IOUtils.toInputStream(content), tempFile, StandardCopyOption.REPLACE_EXISTING);

        // Run.
        String result = reader.getText(tempFile, Charset.defaultCharset());

        // Assert.
        assertEquals(result, content);
    }

    /**
     * Test if the reader removes (only) leading byte order marks.
     */
    @Test
    public void testRemoveBom() throws IOException {
        // Create test file.
        String content = "Byte order mark\uFEFF";
        String contentBom = "\uFEFF\uFEFF" + content;

        // Create a reader and force UTF-8 (otherwise it fails on Windows :( ).
        try (BufferedWriter writer = Files.newBufferedWriter(tempFile, Charset.forName("UTF-8"), StandardOpenOption.WRITE)) {
            writer.write(contentBom);
        }

        // Run.
        String result = reader.getText(tempFile, Charset.forName("UTF-8"));

        // Assert.
        assertEquals(result, content);
    }

    /**
     * Test the process with a file that does not exist.
     */
    @Test(expectedExceptions = OperationFailedException.class)
    public void testFileNotExists() {
        reader.getText(Paths.get("foo"), null);
    }

    /**
     * Test the process with a directory instead of a file.
     */
    @Test(expectedExceptions = OperationFailedException.class)
    public void testFileIsDirectory() throws IOException {
        // Get the system temp directory.
        Path file = Paths.get(System.getProperty("java.io.tmpdir"));

        // Run.
        reader.getText(file, null);
    }
}
