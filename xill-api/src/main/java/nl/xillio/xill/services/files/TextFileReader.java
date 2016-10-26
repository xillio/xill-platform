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
package nl.xillio.xill.services.files;

import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * This interface describes a service that will get text from a file.
 */
public interface TextFileReader {
    /**
     * Get the text from a file.
     *
     * @param source  The source to read the text from.
     * @param charset The charset to use. If this is null, the default charset will be used.
     * @return The text from the source file.
     */
    String getText(Path source, Charset charset);
}
