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

import me.biesaart.utils.IOUtils;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.file.services.files.SimpleTextFileReader;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.*;

import static org.testng.Assert.assertEquals;


public class GetTextConstructTest extends TestUtils {
    private GetTextConstruct construct = new GetTextConstruct(new SimpleTextFileReader());

    @Test
    public void testNormalUsageFromFile() throws IOException {
        // Create test file
        Path file = Files.createTempFile(getClass().getSimpleName(), ".txt");
        Files.copy(IOUtils.toInputStream("File Test"), file, StandardCopyOption.REPLACE_EXISTING);

        setFileResolverReturnValue(file);

        MetaExpression result = ConstructProcessor.process(
                construct.prepareProcess(context(construct)),
                fromValue(file.toAbsolutePath().toString())
        );

        assertEquals(result.getStringValue(), "File Test");

        // Delete test file
        Files.delete(file);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*I don't exist.*")
    public void testFromFileNotExists() {
        // Create test file
        Path file = Paths.get("I don't exist");

        setFileResolverReturnValue(file);

        ConstructProcessor.process(
                construct.prepareProcess(context(construct)),
                fromValue(file.toAbsolutePath().toString())
        );
    }

    @Test(expectedExceptions = NoSuchFileException.class)
    public void testIfStreamClosed() throws IOException {
        // Create test file
        Path file = Files.createTempFile(getClass().getSimpleName(), ".txt");
        Files.copy(IOUtils.toInputStream("File Test"), file, StandardCopyOption.REPLACE_EXISTING);

        setFileResolverReturnValue(file);

        MetaExpression result = ConstructProcessor.process(
                construct.prepareProcess(context(construct)),
                fromValue(file.toAbsolutePath().toString())
        );

        assertEquals(result.getStringValue(), "File Test");

        // Try to delete the test file, this can only happen if the getText actually closed the stream
        Files.delete(file);

        // If the stream is correctly closed, the delete is not queued and we will get a no such file Exception
        // Else you would get an exception when trying to open a stream with the not yet deleted file. in this case the test fails.
        Files.newInputStream(file.toAbsolutePath());
    }
}