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
package nl.xillio.xill.maven.services;

import org.codehaus.plexus.archiver.FileSet;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.assertEquals;

public class FileSetFactoryTest {
    private FileSetFactory factory = new FileSetFactory();

    @Test
    public void testGetFileSet() {
        Path dir = Paths.get(".");
        FileSet fileSet = factory.createFileSet(dir);

        assertEquals(fileSet.getPrefix(), "robots" + File.separator);
        assertEquals(fileSet.getDirectory(), dir.toFile());
    }
}
