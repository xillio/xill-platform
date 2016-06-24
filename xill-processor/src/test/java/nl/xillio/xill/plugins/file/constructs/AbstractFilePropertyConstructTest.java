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
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.assertEquals;


public class AbstractFilePropertyConstructTest extends TestUtils {
    private Path testFile;

    @BeforeClass
    public void createTestFile() throws IOException {
        testFile = Files.createTempFile(getClass().getSimpleName(), ".testfile");
    }

    @AfterClass
    public void deleteTestFile() throws IOException {
        Files.delete(testFile);
    }


    @Test
    public void testProcessNormal() throws Exception {
        AbstractFilePropertyConstruct<String> construct = new SimpleMock();

        ConstructProcessor processor = construct.prepareProcess(context(construct));
        processor.setArgument(0, fromValue(testFile.toString()));
        setFileResolverReturnValue(testFile);

        MetaExpression result = processor.process();

        assertEquals(result.getStringValue(), testFile.toString());
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*iDontExist.*")
    public void testFileNotExists() {
        Path file = Paths.get("iDontExist");

        AbstractFilePropertyConstruct<String> construct = new SimpleMock();

        ConstructProcessor processor = construct.prepareProcess(context(construct));
        processor.setArgument(0, fromValue(file.toString()));
        setFileResolverReturnValue(file);

        processor.process();
    }

    /**
     * Test if IOExceptions are caught and the error message contains the file.
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*testfile.*")
    public void testIOException() {
        AbstractFilePropertyConstruct<String> construct = new IOProcess();

        ConstructProcessor processor = construct.prepareProcess(context(construct));
        processor.setArgument(0, fromValue(testFile.toString()));
        setFileResolverReturnValue(testFile);

        processor.process();
    }

    private static class SimpleMock extends AbstractFilePropertyConstruct<String> {

        @Override
        protected String process(Path path) throws IOException {
            return path.toString();
        }

        @Override
        protected MetaExpression parse(String input) {
            return fromValue(input);
        }
    }

    private static class IOProcess extends SimpleMock {
        @Override
        protected String process(Path path) throws IOException {
            throw new IOException("ERROR");
        }
    }
}