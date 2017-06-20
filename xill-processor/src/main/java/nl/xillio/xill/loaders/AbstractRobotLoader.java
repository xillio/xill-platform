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

import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.io.ResourceLoader;
import xill.RobotLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The abstract base class for robot loaders.
 * This class implements the basic functionality for all robot loaders.
 *
 * @author Luca Scalzotto
 */
public abstract class AbstractRobotLoader implements ResourceLoader, RobotLoader {
    private RobotLoader parent;

    /**
     * Create a new robot loader.
     *
     * @param parent The parent robot loader.
     */
    public AbstractRobotLoader(RobotLoader parent) {
        this.parent = parent;
    }

    @Override
    public RobotLoader getParentLoader() {
        return parent;
    }

    @Override
    public URL getRobot(String fullyQualifiedName) {
        return getResource(fqnToPath(fullyQualifiedName));
    }

    @Override
    public InputStream getRobotAsStream(String fullyQualifiedName) throws IOException {
        return getResourceAsStream(fqnToPath(fullyQualifiedName));
    }

    private String fqnToPath(String fullyQualifiedName) {
        return fullyQualifiedName.replace('.', '/') + XillEnvironment.ROBOT_EXTENSION;
    }

    @Override
    public URL getResource(String path) {
        // If there is a parent, check that first.
        URL resource = parent != null ? parent.getResource(path) : null;

        // Get the resource from this loader.
        return resource == null ? doGetResource(path) : resource;
    }

    @Override
    public InputStream getResourceAsStream(String path) throws IOException {
        URL resource = getResource(path);
        return resource == null ? null : resource.openStream();
    }

    /**
     * Get the resource using this robot loader.
     *
     * @param path the path
     * @return the resource or null if none was found
     */
    protected abstract URL doGetResource(String path);

    @Override
    public void close() throws IOException {
        if (parent != null) {
            parent.close();
        }
    }
}
