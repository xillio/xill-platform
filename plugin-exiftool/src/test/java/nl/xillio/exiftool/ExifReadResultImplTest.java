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
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.testng.Assert.*;


public class ExifReadResultImplTest {
    @Test
    public void testReadResult() {
        BufferedReader bufferedReader = new BufferedReader(new StringReader("======== D:\\File\nTest Value: 5\nOther Value: 234tgr\n{ready}"));
        ExecutionResult executionResult = new ExecutionResult(bufferedReader, () -> {
        }, "{ready}");
        ExifReadResultImpl readResult = new ExifReadResultImpl(executionResult, 10, a -> a);

        assertTrue(readResult.hasNext());

        ExifTags tags = readResult.next();
        ExifTags expected = new ExifTagsImpl();
        expected.put("File Path", "D:\\File");
        expected.put("Test Value", "5");
        expected.put("Other Value", "234tgr");

        assertFalse(readResult.hasNext());
        assertEquals(tags, expected);
    }
}
