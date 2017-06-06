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
package nl.xillio.xill.api.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The interface for retrieving resources (URL and InputStream) for a given path.
 * Since these paths are relative they should not start with a leading slash ("/").
 *
 * All methods of the resource loader will return {@code null} if the resource could not be located.
 * If a resource could in fact be located but a stream could not be created an {@code IOException} will be thrown.
 *
 * @author Pieter Dirk Soels.
 */
public interface ResourceLoader {
    /**
     * Locate a resource for a path.
     *
     * @param path the path
     * @return the resource or null if none was found
     * @throws IllegalArgumentException if the input is not a valid path
     */
    URL getResource(String path);

    /**
     * Locate a resource for a path and open a stream to it.
     *
     * @param path the path
     * @return the resource stream or null if none was found
     * @throws IllegalArgumentException if the input is not a valid path
     * @throws IOException              if the stream could not be opened
     */
    InputStream getResourceAsStream(String path) throws IOException;
}
