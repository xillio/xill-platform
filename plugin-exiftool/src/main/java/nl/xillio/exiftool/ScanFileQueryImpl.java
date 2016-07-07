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
import nl.xillio.exiftool.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * This class provides the main implementation of the ScanFileQuery.
 *
 * @author Thomas Biesaart
 */
class ScanFileQueryImpl extends AbstractQuery<FileQueryOptions, ExifTags> implements ScanFileQuery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanFileQueryImpl.class);

    public ScanFileQueryImpl(Path path, Projection projection, FileQueryOptions options) throws NoSuchFileException {
        super(path, projection, options);

        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException(path + " is not a file");
        }
    }

    @Override
    protected ExifTags buildResult(ExecutionResult executionResult) {
        TagNameConvention nameConvention = getOptions().getTagNameConvention();
        ExifTags result = new ExifTagsImpl();
        result.put(nameConvention.toConvention("File Path"), getPath().toAbsolutePath().toString());

        executionResult.forEachRemaining(
                line -> processLine(line, nameConvention, result)
        );

        return result;
    }

    private void processLine(String line, TagNameConvention convention, ExifTags result) {
        int separator = line.indexOf(":");

        if (separator == -1) {
            LOGGER.error("Failed to parse [{}] as a field", line);
            return;
        }

        String key = line.substring(0, separator).trim();
        String value = line.substring(separator + 1).trim();

        result.put(convention.toConvention(key), value);
    }
}
