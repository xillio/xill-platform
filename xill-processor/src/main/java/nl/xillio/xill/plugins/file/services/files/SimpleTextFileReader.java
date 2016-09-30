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

import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.services.files.TextFileReader;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

public class SimpleTextFileReader implements TextFileReader {
    private static final Pattern LEADING_BOM_PATTERN = Pattern.compile("^\uFEFF+");

    @Override
    public String getText(final Path source, Charset charset) {
        // Check if the source exists, is readable, and is not a directory.
        if (!Files.isReadable(source) || Files.isDirectory(source)) {
            throw new OperationFailedException("open stream for reading", "The file " + source + " is not readable or does not exist.");
        }

        String text;
        if (charset == null) {
            charset = Charset.defaultCharset();
        }

        // Read the text from the file.
        try (InputStream stream = Files.newInputStream(source, StandardOpenOption.READ)) {
            text = IOUtils.toString(stream, charset);
        } catch (IOException e) {
            throw new OperationFailedException("get text from the file", e.getMessage(), e);
        }

        // Replace any leading BOM characters.
        text = LEADING_BOM_PATTERN.matcher(text).replaceFirst("");

        return text;
    }
}
