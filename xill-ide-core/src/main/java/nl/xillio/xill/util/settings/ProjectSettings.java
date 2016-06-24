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
package nl.xillio.xill.util.settings;

/**
 * The class used just like simple read storage for one project settings
 */
public class ProjectSettings {

    private String name;
    private String folder;
    private String description;

    /**
     * @param name        Project name
     * @param folder      Project folder
     * @param description Project description
     */
    public ProjectSettings(final String name, final String folder, final String description) {
        this.name = name;
        this.folder = folder;
        this.description = description;
    }

    /**
     * @return The project name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return The project folder
     */
    public String getFolder() {
        return this.folder;
    }

    /**
     * @return The project description
     */
    public String getDescription() {
        return this.description;
    }
}
