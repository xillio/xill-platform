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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.MetaExpressionIterator;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.file.services.files.FileUtilitiesImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class IterateFoldersConstructTest extends TestUtils {
    private Path testFile;
    private IterateFoldersConstruct construct = new IterateFoldersConstruct();
    private FileUtilitiesImpl fileUtilities = new FileUtilitiesImpl();

    @BeforeClass
    public void createTestFiles() throws IOException {
        testFile = Files.createTempDirectory(getClass().getSimpleName());

        for (int i = 0; i < 4; i++) {
            Path file = testFile.resolve("dir" + i);
            Files.createDirectories(file);
        }

        construct.setFileIterator(fileUtilities);
    }

    @AfterClass
    public void deleteTestFiles() throws IOException {
        fileUtilities.delete(testFile);
    }

    @Test
    public void testNormalFlow() {
        MetaExpression path = fromValue(testFile.toString());

        ConstructProcessor processor = construct.prepareProcess(context(construct));
        processor.setArgument(0, path);

        setFileResolverReturnValue(testFile);
        MetaExpression result = processor.process();

        assertNotNull(result.getMeta(MetaExpressionIterator.class));

        // Count elements
        int count = 0;
        MetaExpressionIterator iterator = result.getMeta(MetaExpressionIterator.class);
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        assertEquals(count, 4);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*I don't exist.*")
    public void testFileNotExist() {
        Path noExistFile = Paths.get("I don't exist");

        ConstructProcessor processor = construct.prepareProcess(context(construct));
        processor.setArgument(0, fromValue(noExistFile.toString()));
        setFileResolverReturnValue(noExistFile);

        processor.process();
    }
}