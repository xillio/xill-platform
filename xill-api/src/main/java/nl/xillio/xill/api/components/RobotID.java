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

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * A unique identifier for robots.
 */
public class RobotID implements Serializable {
    private static Map<String, RobotID> ids = new Hashtable<>();
    private final File projectPath;

    private static Map<URI, RobotID> urlIds = new Hashtable<>();
    private final URI uri;
    private final String name;

    private RobotID(final File projectPath, final URI uri) {
        this.projectPath = projectPath;
        this.uri = uri;
        URI testuri = URI.create("xip:file:///dings/dangs.xip!/iets.xill");
        name = testuri.getPath();
        String dinges = "dignes";
    }

    private RobotID(final URI uri){
        this.projectPath = null;
        this.uri = uri;
        name = uri.getPath();
    }

    /**
     * Returns the path associated with this id.
     *
     * @return the path associated with this id
     */
    public File getPath() {
        return new File(uri.getPath());
    }

    public URI getURI(){ return uri;}

    @Override
    public String toString() {
        return uri.toString();
    }

    /**
     * Gets/creates a robotID that is singular for every path.
     *
     * @param file        the robot file
     * @param projectPath the path to the root folder of the workspace
     * @return a unique robot id for this path
     */
    public static RobotID getInstance(final File file, final File projectPath) {

        String identity = file.getAbsolutePath() + "in" + projectPath.getAbsolutePath();
        RobotID id = ids.get(identity);
        if (id == null) {
            id = new RobotID(projectPath, URI.create("file:///" + file.getAbsolutePath().replaceAll("\\\\", "/")));
            ids.put(identity, id);
        }
        return id;
    }


    public static RobotID getURIInstance(final URI uri) {
        RobotID id = urlIds.get(uri);
        if (id == null) {
            id = new RobotID(uri);
            urlIds.put(uri, id);
        }
        return id;
    }

    /**
     * @return the projectPath
     */
    public File getProjectPath() {
        return projectPath;
    }

    /**
     * Used in tests to create a dummy ID.
     *
     * @return a dummy IDfor testing.
     */
    public static RobotID dummyRobot() {
        try {
            return new RobotID(new File("."), new URI("."));
        }
        catch (URISyntaxException e){

        }
        return null;
    }
}
