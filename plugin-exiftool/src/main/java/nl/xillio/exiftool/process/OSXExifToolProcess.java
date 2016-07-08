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


import java.io.IOException;

/**
 * This is the osx implementation of the {@link ExifToolProcess}.
 *
 * @author Thomas Biesaart
 */
public class OSXExifToolProcess extends UnixExifToolProcess {

    @Override
    protected Process buildProcess(ProcessBuilder processBuilder) throws IOException {
        String exifBin = System.getenv("exiftool_bin");
        if (exifBin ==null) {
            exifBin = searchExiftoolOnPath();
        }
        if (exifBin == null) {
            exifBin = "/usr/local/bin/exiftool";
        }

        processBuilder.command(exifBin, "-stay_open", "True", "-@", "-");
        return processBuilder.start();
    }
}
