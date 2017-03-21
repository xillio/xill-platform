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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link RobotLoader} which loads resources from a given xip archive.
 * While the loader is active there will be a write lock on the archive.
 * This lock is released when the loader is closed.
 *
 * @author Luca Scalzotto
 */
public class XipRobotLoader extends AbstractRobotLoader {
    private static final Logger LOGGER = Log.get();

    private final Path xipFile;
    private final FileSystem archiveFs;

    /**
     * Create a new xip robot loader.
     *
     * @param parent  The parent robot loader.
     * @param xipFile The xip archive to load resources from.
     * @throws IOException if an exception occurred while opening the archive
     */
    public XipRobotLoader(RobotLoader parent, Path xipFile) throws IOException {
        super(parent);
        this.xipFile = xipFile.toAbsolutePath().normalize();
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        archiveFs = FileSystems.newFileSystem(URI.create("jar:" + this.xipFile.toUri()), env);
    }

    @Override
    protected URL doGetResource(String path) {
        Path pathInArchive = archiveFs.getPath(path).normalize();
        return Files.exists(pathInArchive) ? createUrl(pathInArchive) : null;
    }

    private URL createUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed url for archive: " + xipFile.toString() + " and file: " + path, e);
            return null;
        }
    }

    @Override
    public InputStream getResourceAsStream(String path) throws IOException {
        URL resource = getResource(path);
        if (resource == null) {
            return null;
        }

        // Disable the caching, otherwise you can't delete the archive on Windows.
        URLConnection uc = resource.openConnection();
        uc.setUseCaches(false);
        return uc.getInputStream();
    }

    /**
     * Close the robot loader.
     * This also releases the write lock on the archive.
     *
     * @throws IOException if an I/O exception occurred
     */
    @Override
    public void close() throws IOException {
        archiveFs.close();
        super.close();
    }
}
