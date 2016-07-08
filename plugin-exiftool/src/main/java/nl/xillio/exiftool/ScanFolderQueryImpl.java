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
import nl.xillio.exiftool.query.ExifReadResult;
import nl.xillio.exiftool.query.FolderQueryOptions;
import nl.xillio.exiftool.query.Projection;
import nl.xillio.exiftool.query.ScanFolderQuery;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * This class represents the default implementation of the ScanFolderQuery.
 *
 * @author Thomas Biesaart
 */
class ScanFolderQueryImpl extends AbstractQuery<FolderQueryOptions, ExifReadResult> implements ScanFolderQuery {


    public ScanFolderQueryImpl(Path folder, Projection projection, FolderQueryOptions folderQueryOptions) throws NoSuchFileException {
        super(folder, projection, folderQueryOptions);

        if (!Files.isDirectory(folder)) {
            throw new IllegalArgumentException(folder + " is not a folder");
        }
    }

    @Override
    protected ExifReadResult buildResult(ExecutionResult executionResult) {
        return new ExifReadResultImpl(executionResult, 100, getOptions().getTagNameConvention());
    }
}
