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
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.plugins.date.services.DateServiceImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import static org.testng.Assert.assertEquals;


public class AbstractDateFilePropertyConstructTest extends TestUtils {
    private Path testFile;
    private AbstractDateFilePropertyConstruct construct = new MockConstruct();

    @BeforeClass
    public void createTestFile() throws IOException {
        testFile = Files.createTempFile(getClass().getSimpleName(), ".testfile");
    }

    @AfterClass
    public void deleteTestFile() throws IOException {
        Files.delete(testFile);
    }


    @Test
    public void testParse() throws Exception {
        FileTime fileTime = FileTime.fromMillis(100);
        construct.setDateFactory(new DateServiceImpl());
        MetaExpression dateValue = construct.parse(fileTime);

        assertEquals(dateValue.getMeta(Date.class).getZoned().toInstant(), fileTime.toInstant());
    }

    @Test
    public void testAttributes() throws Exception {
        FileTime result = construct.process(testFile);

        assertEquals(result, Files.readAttributes(testFile, BasicFileAttributes.class).creationTime());
    }

    private class MockConstruct extends AbstractDateFilePropertyConstruct {
        @Override
        protected FileTime process(Path path) throws IOException {
            return attributes(path).creationTime();
        }
    }
}