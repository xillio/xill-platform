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


import java.io.File;
import java.io.IOException;

/**
 * This is the linux implementation of the {@link ExifToolProcess}.
 *
 * @author Thomas Biesaart
 */
public class UnixExifToolProcess extends AbstractExifToolProcess {

    @Override
    protected Process buildProcess(ProcessBuilder processBuilder) throws IOException {
        String exifBin = System.getenv("exiftool_bin");
        if (exifBin == null) {
            exifBin = searchExiftoolOnPath();
        }

        if (exifBin == null || !exist(exifBin, true)) {
            throw new IOException("Please set your exiftool_bin environmental variable to the path to your exiftool installation.");
        }

        processBuilder.command(exifBin, "-stay_open", "True", "-@", "-");

        String perlBin = System.getenv("perl_bin");
        if (perlBin != null) {
            if (!exist(exifBin, true)) {
                throw new IOException("Please set correct perl_bin environmental variable.");
            }
            processBuilder.command().add(0, perlBin);
        }

        return processBuilder.start();
    }

    private boolean exist(final String fileName, boolean binary) {
        final File file = new File(fileName);
        if (!file.isFile()) {
            return false;
        }
        if (binary && !file.canExecute()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean needInit() {
        return false;
    }

    @Override
    public void init() throws IOException {
        // The user has to manually install Exiftool
    }
}
