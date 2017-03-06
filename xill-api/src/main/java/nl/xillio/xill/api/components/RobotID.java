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
import java.util.Hashtable;
import java.util.Map;

/**
 * A unique identifier for robots.
 */
public class RobotID implements Serializable {
    private static Map<String, RobotID> ids = new Hashtable<>();
    private final File path;

    private RobotID(final File path) {
        this.path = path;
    }

    /**
     * Returns the path associated with this id.
     *
     * @return the path associated with this id
     */
    public File getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path.getAbsolutePath();
    }

    /**
     * Gets/creates a robotID that is singular for every path.
     *
     * @param file  the robot file
     * @return a unique robot id for this path
     */
    public static RobotID getInstance(final File file) {
        String identity = file.getAbsolutePath();

        RobotID id = ids.get(identity);

        if (id == null) {
            id = new RobotID(file);
            ids.put(identity, id);
        }

        return id;
    }

    /**
     * Used in tests to create a dummy ID.
     *
     * @return a dummy ID for testing.
     */
    public static RobotID dummyRobot() {
        return new RobotID(new File("."));
    }
}
