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
package nl.xillio.xill.plugins.system.services.info;

import com.google.common.util.concurrent.Service;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.services.PropertiesProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents a {@link Service} that provides information about the currently running robot
 */
public class RobotRuntimeInfo implements PropertiesProvider {

    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final String robotPath;
    private final String rootRobotPath;
    private final String workingDirectory;

    /**
     * Create a new {@link RobotRuntimeInfo} for a {@link ConstructContext}
     *
     * @param context the context
     */
    public RobotRuntimeInfo(final ConstructContext context) {
        robotPath = context.getRobotID().getURL().toString();
        rootRobotPath = context.getRootRobot().getURL().toString();
        workingDirectory = context.getWorkingDirectory().toString();

        properties.put("robotPath", robotPath);
        properties.put("rootRobotPath", rootRobotPath);
        properties.put("workingDirectory", workingDirectory);
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * @return the robotPath
     */
    public String getRobotPath() {
        return robotPath;
    }

    /**
     * @return the rootRobotPath
     */
    public String getRootRobotPath() {
        return rootRobotPath;
    }

    /**
     * @return the projectPath
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

}
