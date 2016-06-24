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
package nl.xillio.xill.plugins.system.exec;

import java.io.File;

/**
 * This class represents the information requried by the {@link ProcessFactory} to build a process
 */
public class ProcessDescription {
    private static final String DEFAULT_LABEL = "System.exec";

    private final String[] commands;
    private final File workdingDirectory;
    private String friendlyName = DEFAULT_LABEL;

    /**
     * Create a new {@link ProcessDescription}
     *
     * @param workdingDirectory the working directory or null
     * @param commands          the commands to run
     */
    public ProcessDescription(final File workdingDirectory, final String... commands) {
        this.workdingDirectory = workdingDirectory;
        this.commands = commands;
    }

    /**
     * @return the commands
     */
    public String[] getCommands() {
        return commands;
    }

    /**
     * @return the workdingDirectory
     */
    public File getWorkdingDirectory() {
        return workdingDirectory;
    }

    /**
     * @return the friendlyName
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * @param friendlyName the friendlyName to set
     */
    public void setFriendlyName(final String friendlyName) {
        this.friendlyName = friendlyName;
    }
}
