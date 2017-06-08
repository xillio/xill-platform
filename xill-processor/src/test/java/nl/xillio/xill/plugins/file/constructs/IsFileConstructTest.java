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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import static org.testng.Assert.*;

public class IsFileConstructTest extends TestUtils {
    private Path testFile;
    private Path testFolder;
    private IsFileConstruct construct = new IsFileConstruct();

    @BeforeClass
    public void createTestFile() throws IOException {
        testFile = Files.createTempFile(getClass().getSimpleName(), ".txt");
        testFolder = Files.createTempDirectory(getClass().getSimpleName());
    }

    @AfterClass
    public void deleteTestFile() throws IOException {
        Files.delete(testFile);
        Files.delete(testFolder);
    }


    //Test using a path that exists and is a file
    @Test
    public void testProcessNormalFile(){

        MetaExpression path = fromValue(testFile.toString());

        ConstructProcessor processor = construct.prepareProcess(context(construct));
        processor.setArgument(0, path);

        setFileResolverReturnValue(testFile);
        MetaExpression result = processor.process();

        //Should return true
        assertTrue(result.getBooleanValue());
    }

    @Test
    public void testProcessNormalFolder(){
        MetaExpression path = fromValue(testFolder.toString());

        ConstructProcessor processor = construct.prepareProcess(context(construct));
        processor.setArgument(0, path);

        setFileResolverReturnValue(testFolder);
        MetaExpression result = processor.process();

        //Should return false
        assertFalse(result.getBooleanValue());
    }

    @Test
    public void testProcessNonExistingFile(){
        MetaExpression path = fromValue(getClass().getSimpleName() + "/thisDoesNotExists");

        ConstructProcessor processor = construct.prepareProcess(context(construct));
        processor.setArgument(0, path);

        setFileResolverReturnValue(testFolder);
        MetaExpression result = processor.process();

        //Should return false
        assertFalse(result.getBooleanValue());
    }
}