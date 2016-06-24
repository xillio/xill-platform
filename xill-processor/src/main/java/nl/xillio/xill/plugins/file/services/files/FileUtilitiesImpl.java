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
import nl.xillio.xill.plugins.file.services.FileSizeCalculator;
import nl.xillio.xill.plugins.file.services.FileSystemIterator;
import nl.xillio.xill.plugins.file.utils.FileIterator;
import nl.xillio.xill.plugins.file.utils.FolderIterator;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;

/**
 * This is the main implementation of the {@link FileUtilities} service.
 */
@Singleton
public class FileUtilitiesImpl implements FileUtilities, FileSizeCalculator, FileSystemIterator {

    @Override
    public void copy(final Path source, final Path target) throws IOException {
        if (!Files.exists(source)) {
            throw new NoSuchFileException(source.toAbsolutePath().toString());
        }


        if (Files.isDirectory(source)) {
            copyDirectory(source, target);
        } else {
            createDir(target.getParent());
            copyFile(source, target);
        }
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relative = source.relativize(file);
                copyFile(file, target.resolve(relative));
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relative = source.relativize(dir);
                Path targetDir = target.resolve(relative);
                createDir(targetDir);
                return super.preVisitDirectory(dir, attrs);
            }
        });
    }

    private void copyFile(Path source, Path target) throws IOException {
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    private void createDir(Path path) throws IOException {
        if (path != null) {
            Files.createDirectories(path);
        }
    }

    @Override
    public void createFolder(final Path folder) throws IOException {
        if (Files.isRegularFile(folder)) {
            throw new FileAlreadyExistsException("A file already exists at " + folder.toAbsolutePath());
        }
        Files.createDirectories(folder);
    }

    @Override
    public boolean exists(final Path file) {
        return Files.exists(file);
    }

    @Override
    public void delete(final Path file) throws IOException {
        if (!Files.exists(file)) {
            return;
        }

        Files.walkFileTree(file, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    @Override
    public Iterator<Path> iterateFiles(Path folder, boolean recursive) throws IOException {
        return new FileIterator(folder, recursive);
    }

    @Override
    public Iterator<Path> iterateFolders(Path folder, boolean recursive) throws IOException {
        return new FolderIterator(folder, recursive);
    }

    @Override
    public long getSize(Path path) throws IOException {
        if (Files.isRegularFile(path)) {
            return Files.size(path);
        } else {
            FileSizeWalker walker = new FileSizeWalker();
            Files.walkFileTree(path, walker);
            return walker.getSize();
        }
    }

    private static class FileSizeWalker extends SimpleFileVisitor<Path> {
        private long size = 0;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            size += Files.size(file);
            return super.visitFile(file, attrs);
        }

        public long getSize() {
            return size;
        }
    }
}
