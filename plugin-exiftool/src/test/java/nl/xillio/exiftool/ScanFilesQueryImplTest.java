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
package nl.xillio.exiftool;

import nl.xillio.exiftool.process.ExecutionResult;
import nl.xillio.exiftool.query.ExifTags;
import nl.xillio.exiftool.query.Projection;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;


public class ScanFilesQueryImplTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructorConstraintFile() throws IOException {
        Path file = Files.createTempDirectory("unittest");
        new ScanFileQueryImpl(file, new Projection(), new FileQueryOptionsImpl());
        Files.delete(file);
    }

    @Test(expectedExceptions = NoSuchFileException.class)
    public void testConstructorConstraintNotExist() throws NoSuchFileException {
        new ScanFileQueryImpl(Paths.get("I surely do not exist"), null, null);
    }

    @Test
    public void testBuildResult() throws IOException {
        Path file = Files.createTempFile("unittest", "test");
        ExecutionResult result = mock(ExecutionResult.class);
        when(result.hasNext()).thenReturn(true, true, true, true, false);
        when(result.next()).thenReturn("value1:true", "value2:false", "value3:null", "I CANNOT BE PARSED");
        doCallRealMethod().when(result).forEachRemaining(any());

        ScanFileQueryImpl query = new ScanFileQueryImpl(file, new Projection(), new FileQueryOptionsImpl());

        ExifTags tags = query.buildResult(result);

        assertEquals(tags.size(), 4);
        assertEquals(new ArrayList<>(tags.values()).subList(1, tags.size()), Arrays.asList("true", "false", "null"));
    }


}
