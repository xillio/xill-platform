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

import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class that contains common methods for storing the project settings
 *
 * @author Zbynek Hochmann
 */
public class ProjectSettingsHandler {

    private static final String CATEGORY = "project";
    private static final String NAME = "name";
    private static final String KEYNAME = NAME;
    private static final String FOLDER = "folder";
    private static final String DESCRIPTION = "description";

    private ContentHandler content;

    private static final Logger LOGGER = Log.get();

    ProjectSettingsHandler(ContentHandler content) {// Can be instantiated within package only
        this.content = content;
    }

    /**
     * Store the given project to settings.
     *
     * @param project The project data
     */
    public void save(ProjectSettings project) {
        HashMap<String, Object> itemContent = new HashMap<>();
        itemContent.put(NAME, project.getName());
        itemContent.put(FOLDER, project.getFolder());
        itemContent.put(DESCRIPTION, project.getDescription());

        this.content.set(CATEGORY, itemContent, KEYNAME, project.getName());
    }

    /**
     * @return List of all projects in settings
     */
    public List<ProjectSettings> getAll() {
        LinkedList<ProjectSettings> list = new LinkedList<>();

        try {
            List<Map<String, Object>> result = this.content.getAll(CATEGORY);
            for (Map<String, Object> item : result) {
                list.add(new ProjectSettings(item.get(NAME).toString(), item.get(FOLDER).toString(), item.get(DESCRIPTION).toString()));
            }
        } catch (IOException e) {
            LOGGER.error("Invalid file structure for settings file", e);
        }
        return list;
    }

    /**
     * Deletes the project from settings (according to its the project name)
     *
     * @param name The name of the project
     */
    public void delete(final String name) {
        this.content.delete(CATEGORY, KEYNAME, name);
    }
}
