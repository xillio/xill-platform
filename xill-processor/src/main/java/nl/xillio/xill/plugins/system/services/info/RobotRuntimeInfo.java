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
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.services.PropertiesProvider;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class represents a {@link Service} that provides information about the currently running robot
 */
public class RobotRuntimeInfo implements PropertiesProvider {
    private static final Pattern FQN_PATTERN = Pattern.compile("[A-z_][0-9A-z_]*(\\.[A-z_][0-9A-z_]*)*");

    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final String robotPath;
    private final String rootRobotPath;
    private final String workingDirectory;
    private final String resourcePath;
    private final String qualifiedName;

    /**
     * Create a new {@link RobotRuntimeInfo} for a {@link ConstructContext}
     *
     * @param context the context
     */
    public RobotRuntimeInfo(final ConstructContext context) {
        robotPath = context.getRobotID().getURL().toString();
        rootRobotPath = context.getRootRobot().getURL().toString();
        workingDirectory = context.getWorkingDirectory().toString();
        resourcePath = context.getRobotID().getResourcePath();
        qualifiedName = getQualifiedName(context.getRobotID());

        properties.put("robotPath", robotPath);
        properties.put("resourcePath", resourcePath);
        properties.put("qualifiedName", qualifiedName);
        properties.put("rootRobotPath", rootRobotPath);
        properties.put("workingDirectory", workingDirectory);
    }

    private String getQualifiedName(RobotID robotID) {
        String result = robotID.getResourcePath().replaceAll("[/\\\\]", ".");
        if (result.endsWith(XillEnvironment.ROBOT_EXTENSION)) {
            result = result.substring(0, result.length() - XillEnvironment.ROBOT_EXTENSION.length());
        }
        if (FQN_PATTERN.matcher(result).matches()) {
            return result;
        }
        return null;
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
     * @return the resource path
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * @return the robot's qualified name or null
     */
    public String getQualifiedName() {
        return qualifiedName;
    }

    /**
     * @return the projectPath
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

}
