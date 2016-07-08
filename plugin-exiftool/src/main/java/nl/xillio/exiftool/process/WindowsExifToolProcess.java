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
package nl.xillio.exiftool.process;


import nl.xillio.exiftool.ProcessPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * This is the Windows implementation of the {@link ExifToolProcess}.
 *
 * @author Thomas Biesaart
 */
public class WindowsExifToolProcess extends AbstractExifToolProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsExifToolProcess.class);
    private static final URL EMBEDDED_BINARY = ProcessPool.class.getResource("/exiftool(-k).exe");
    private final Path nativeBinary;

    public WindowsExifToolProcess(Path nativeBinary) {
        Objects.requireNonNull(nativeBinary);
        this.nativeBinary = nativeBinary.toAbsolutePath();
    }

    @Override
    public boolean needInit() {
        return !Files.exists(nativeBinary);
    }


    @Override
    public void init() throws IOException {
        if (!needInit()) {
            return;
        }

        Files.createDirectory(nativeBinary.getParent());
        Files.createFile(nativeBinary);

        LOGGER.info("Deploying exiftool binary to " + nativeBinary);
        try (InputStream stream = EMBEDDED_BINARY.openStream()) {
            Files.copy(stream, nativeBinary, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    protected Process buildProcess(ProcessBuilder processBuilder) throws IOException {
        processBuilder.command(nativeBinary.toString(), "-stay_open", "True", "-@", "-");
        return processBuilder.start();
    }
}
