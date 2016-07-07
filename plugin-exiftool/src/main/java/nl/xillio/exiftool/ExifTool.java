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

import nl.xillio.exiftool.process.ExifToolProcess;
import nl.xillio.exiftool.process.OSXExifToolProcess;
import nl.xillio.exiftool.process.UnixExifToolProcess;
import nl.xillio.exiftool.process.WindowsExifToolProcess;
import nl.xillio.exiftool.query.*;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * This class represents an easy-to-use facade around the ExifTool wrapper library.
 *
 * @author Thomas Biesaart
 */
public class ExifTool implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExifTool.class);

    private final ExifToolProcess process;
    private final Consumer<ExifToolProcess> releaseMethod;

    ExifTool(ExifToolProcess process, Consumer<ExifToolProcess> releaseMethod) {
        this.process = process;
        this.releaseMethod = releaseMethod;
    }

    @Override
    public void close() {
        LOGGER.debug("Releasing exiftool process");
        releaseMethod.accept(process);
    }

    public static ProcessPool buildPool(Path windowsBinaryLocation) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return new ProcessPool(() -> new WindowsExifToolProcess(windowsBinaryLocation));
        }

        if (SystemUtils.IS_OS_MAC) {
            return new ProcessPool(OSXExifToolProcess::new);
        }

        if (SystemUtils.IS_OS_UNIX) {
            return new ProcessPool(UnixExifToolProcess::new);
        }

        throw new NotImplementedException("No implementation for " + SystemUtils.OS_NAME);
    }

    public static ProcessPool buildPool() {
        return buildPool(null);
    }

    public ExifReadResult readFieldsForFolder(Path path, Projection projection, FolderQueryOptions folderQueryOptions) throws IOException {
        LOGGER.info("Reading tags for files in " + path);

        ScanFolderQuery scanFolderQuery = new ScanFolderQueryImpl(path, projection, folderQueryOptions);

        return scanFolderQuery.run(process);
    }

    public ExifTags readFieldsForFile(Path file, Projection projection, FileQueryOptions options) throws IOException {
        LOGGER.info("Reading tags for " + file);
        ScanFileQuery scanFileQuery = new ScanFileQueryImpl(file, projection, options);
        return scanFileQuery.run(process);
    }
}
