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

import com.google.inject.Singleton;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.io.IOStream;
import nl.xillio.xill.api.io.SimpleIOStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * This class builds IOStreams from Paths.
 *
 * @author Thomas biesaart
 */
@Singleton
public class FileStreamFactory {

    public IOStream openAppend(Path path) throws IOException {
        assertNotDirectory(path, "append");

        if (!Files.isWritable(path) && Files.exists(path)) {
            throw new OperationFailedException("open stream for appending", "The path " + path + " is not writable or does not exist.");
        }

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        return new SimpleIOStream(Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND), path.toString());
    }

    public IOStream openRead(Path path) throws IOException {
        assertNotDirectoryAndExists(path, "read");

        if (!Files.isReadable(path)) {
            throw new OperationFailedException("open stream for reading", "The path " + path + " is not readable.");
        }

        return new SimpleIOStream(Files.newInputStream(path, StandardOpenOption.READ), path.toString());
    }

    public IOStream openWrite(Path path) throws IOException {
        assertNotDirectory(path, "write");

        if (!Files.isWritable(path) && Files.exists(path)) {
            throw new OperationFailedException("open stream for writing", "The path " + path + " is not writable or does not exist.");
        }

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        return new SimpleIOStream(Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), path.toString());
    }

    private void assertNotDirectoryAndExists(Path target, String targetType) {
        assertExists(target);

        assertNotDirectory(target, targetType);
    }

    private void assertNotDirectory(Path target, String targetType) {
        if (Files.isDirectory(target)) {
            throw new OperationFailedException("create " + targetType + " target for a directory", target.toFile().getAbsolutePath() + " is not a directory.");
        }
    }

    private void assertExists(Path target) {
        if (!Files.exists(target)) {
            throw new OperationFailedException("find " + target, "The target does not exist.");
        }
    }
}
