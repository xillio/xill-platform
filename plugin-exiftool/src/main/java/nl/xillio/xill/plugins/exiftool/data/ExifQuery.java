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
package nl.xillio.xill.plugins.exiftool.data;

import nl.xillio.exiftool.ExifTool;
import nl.xillio.xill.api.data.MetadataExpression;

/**
 * This class represents a wrapper around the resources needed for an exiftool query.
 *
 * @author Thomas Biesaart
 */
public class ExifQuery implements AutoCloseable, MetadataExpression {
    private final ExifTool exifTool;

    public ExifQuery(ExifTool exifTool) {
        this.exifTool = exifTool;
    }

    @Override
    public void close() {
        exifTool.close();
    }
}
