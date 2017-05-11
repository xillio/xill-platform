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
package nl.xillio.xill.loaders;

import me.biesaart.utils.Log;
import org.slf4j.Logger;
import xill.RobotLoader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A {@link RobotLoader} which loads resources from a given directory.
 *
 * @author Luca Scalzotto
 */
public class DirectoryRobotLoader extends AbstractRobotLoader {
    private static final Logger LOGGER = Log.get();

    private final Path directory;

    /**
     * Create a new directory robot loader.
     *
     * @param parent    The parent robot loader.
     * @param directory The directory to load resources from.
     */
    public DirectoryRobotLoader(RobotLoader parent, Path directory) {
        super(parent);
        this.directory = directory.toAbsolutePath().normalize();
    }

    @Override
    protected URL doGetResource(String path) {
        // Try to get the real path, this will throw an error if it does not exist.
        Path resource = directory.resolve(path).toAbsolutePath().normalize();

        // Check if the resource is not above the loader directory.
        if (!resource.startsWith(directory)) {
            return null;
        }

        try {
            // Check if the case matches, use String::endsWith instead of Path::endsWith to ensure case sensitivity.
            return resource.toRealPath().toString().endsWith(Paths.get(path).normalize().toString()) ? resource.toUri().toURL() : null;
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed url for path: " + resource, e);
            return null;
        } catch (IOException e) {
            LOGGER.error("Could not get real path for path: " + path, e);
            return null;
        }
    }
}
