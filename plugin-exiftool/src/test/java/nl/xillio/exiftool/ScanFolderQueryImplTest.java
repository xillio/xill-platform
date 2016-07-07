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

import nl.xillio.exiftool.query.Projection;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ScanFolderQueryImplTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructorConstraintFile() throws IOException {
        Path file = Files.createTempFile("unittest", "");
        new ScanFolderQueryImpl(file, new Projection(), new FolderQueryOptionsImpl());
        Files.delete(file);
    }

    @Test(expectedExceptions = NoSuchFileException.class)
    public void testConstructorConstraintNotExist() throws NoSuchFileException {
        new ScanFolderQueryImpl(Paths.get("I surely do not exist"), null, null);
    }

}
