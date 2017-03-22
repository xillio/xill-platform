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
package nl.xillio.xill.api.components;

import nl.xillio.xill.api.XillEnvironment;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * A unique identifier for robots.
 *
 * A RobotID is composed of two components: The robot url and a resource uri.
 *
 * The <b>robot url</b>, also called <code>url</code>, is an absolute url to the robot resource such that the resource
 * can be opened and read to get the robot source code.
 *
 * The <b>resource uri</b>, or <code>resourceUri</code>, is represented by an {@link URI} relative to the resource root.
 * This resource root is the root of the parent container of resources. Examples of this could be an archive, folder or
 * network share.
 *
 * The example below describes how these options are defined for a project on the hard drive at "/my/project":
 *
 * <code>
 *     // Create the robot url
 *     URL robotUrl = new URL("file:///my/project/libs/URLParser.xill");
 *
 *     // Create the resourceUri relative to the project root
 *     URI resourceUri = URI.create("libs/URLParser.xill");
 *
 *     // Create the RobotID
 *     RobotID robotId = new RobotID(robotUrl, resourceUri);
 * </code>
 */
public class RobotID implements Serializable {
    private final URL url;
    private final URI resourceUri;

    /**
     * Create a new robotID from an url and a resource uri.
     *
     * @param url           The url to this robot
     * @param resourceUri   The robot uri relative to the resource root
     * @throws IllegalArgumentException if resourceUri is not a relative uri
     */
    public RobotID(URL url, URI resourceUri) {
        notNull(url);
        notNull(resourceUri);

        if (resourceUri.isAbsolute() ||
                resourceUri.getScheme() != null ||
                resourceUri.getAuthority() != null) {
            throw new IllegalArgumentException(resourceUri + " is not a relative uri");
        }

        this.url = url;
        this.resourceUri = resourceUri;
    }

    public URL getURL() {
        return url;
    }

    public URI getResourceUri() {
        return resourceUri;
    }

    public String getResourcePath() {
        return resourceUri.toString();
    }

    @Override
    public String toString() {
        return "RobotID{" +
                "url=" + url +
                ", resourceUri=" + resourceUri +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RobotID robotID = (RobotID) o;
        return Objects.equals(url, robotID.url) &&
                Objects.equals(resourceUri, robotID.resourceUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, resourceUri);
    }

    private static URL createUrl(URL baseUrl, String resourcePath) {
        try {
            return new URL(baseUrl, resourcePath);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Build a RobotID from a baseUrl and a resourcePath.
     *
     * @param baseUrl       The url to the resource root that contains this robot
     * @param resourcePath  The robot uri relative to the baseUrl
     * @return the RobotID
     */
    public static RobotID build(String baseUrl, String resourcePath) {
        return new RobotID(createUrl(createUrl(null, baseUrl), resourcePath), URI.create(resourcePath));
    }

    /**
     * Build a dummy ID that points to an arbitrary robot.
     *
     * @return a dummy robotID
     */
    public static RobotID dummyRobot() {
        return build("file:///path/to/project", "my/Robot.xill");
    }

    /**
     * Convert a qualified name to a path.
     *
     * @param fullyQualifiedName    the qualified name to convert to a path
     * @return the converted qualified name to a path
     */
    public static String qualifiedNameToPath(String fullyQualifiedName) {
        return fullyQualifiedName.replace('.', '/') + XillEnvironment.ROBOT_EXTENSION;
    }
}
