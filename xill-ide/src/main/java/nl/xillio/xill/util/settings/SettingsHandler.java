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

import me.biesaart.utils.FileUtils;
import me.biesaart.utils.Log;
import nl.xillio.util.XillioHomeFolder;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Class that is main point for dealing with settings in Xill IDE
 * It encapsulates all settings handlers.
 *
 * @author Zbynek Hochmann
 */
public class SettingsHandler {

    private static final String SETTINGS_FILE_NAME = "settings.cfg";
    private static final File SETTINGS_FILE = new File(XillioHomeFolder.forXillIDE(), SETTINGS_FILE_NAME);
    private static final File SETTINGS_FILE_BACKUP = new File(XillioHomeFolder.forXillIDE(), SETTINGS_FILE_NAME + ".bak");
    private static final Logger LOGGER = Log.get();

    private static SettingsHandler settings;
    private ContentHandlerImpl content;
    private SimpleVariableHandler simple;
    private ProjectSettingsHandler project;

    private SettingsHandler() throws IOException {// singleton class

        this.content = new ContentHandlerImpl(SETTINGS_FILE);
        this.content.init();

        this.simple = new SimpleVariableHandler(this.content);
        this.project = new ProjectSettingsHandler(this.content);
    }

    /**
     * @return The instance of settings handler
     */
    public static SettingsHandler getSettingsHandler() {
        return settings;
    }

    /**
     * Load settings from the settings file.
     * <p>
     * This method should be called once before the settings are used and can be called when the settings need to be reloaded.
     * <p>
     * When loading of settings succeeds, a backup is written. This backup can be recoverd using {@link SettingsHandler#recoverSettings()}
     *
     * @throws IOException When the settings file cannot be parsed
     */
    public static void loadSettings() throws IOException {
        settings = new SettingsHandler();

        // Write a backup copy of the settings
        // Exceptions thrown here should not be propagated (the IDE should just continue loading)
        try {
            FileUtils.copyFile(SETTINGS_FILE, SETTINGS_FILE_BACKUP);
        } catch (IOException e) {
            LOGGER.error("Could not write settings backup", e);
        }
    }

    /**
     * Overwrite the settings file by a previously created backup
     * @throws IOException When recovery fails
     */
    public static void recoverSettings() throws IOException {
        FileUtils.copyFile(SETTINGS_FILE_BACKUP, SETTINGS_FILE);
    }

    /**
     * Overwrite the current settings by the defaults and load them
     * @throws IOException When writing default settings or loading them fails
     */
    public static void forceDefaultSettings() throws IOException {
        FileUtils.forceDelete(SETTINGS_FILE);
        loadSettings();
    }

    /**
     * @return The implementation of simple variable settings
     */
    public SimpleVariableHandler simple() {
        return this.simple;
    }

    /**
     * @return The implementation of project settings
     */
    public ProjectSettingsHandler project() {
        return this.project;
    }

    /**
     * Set the save mechanism (see {@link nl.xillio.xill.util.settings.ContentHandler#setManualCommit(boolean)})
     *
     * @param manual true = manual commit, false = auto commit (default)
     */
    public void setManualCommit(boolean manual) {
        this.content.setManualCommit(manual);
    }

    /**
     * Save all changes from last commit() if manual commit is on (see {@link nl.xillio.xill.util.settings.ContentHandler#commit()})
     */
    public void commit() {
        this.content.commit();
    }
}
