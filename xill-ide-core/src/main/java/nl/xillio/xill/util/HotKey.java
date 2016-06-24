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
package nl.xillio.xill.util;

import com.sun.javafx.tk.Toolkit;
import javafx.scene.input.KeyCode;

/**
 * This class represents a hotkey that triggers an action.
 */
class Hotkey {
    private static final String SYSTEM_SPECIFIC_SHORTCUT_TEXT = Toolkit.getToolkit().getPlatformShortcutKey().getName();

    private String shortcut;
    private String settingsid;
    private String settingsDscr;
    private String fxid;

    /**
     * Create a new Hotkey
     *
     * @param shortcut     the hotkey pattern
     * @param settingsid   the id that should be used in the settings
     * @param settingsDscr the description that should be used in the settings
     * @param fxid         the id that is used in the fxml definition
     */
    public Hotkey(String shortcut, String settingsid, String settingsDscr, String fxid) {
        this.shortcut = shortcut;
        this.settingsid = settingsid;
        this.settingsDscr = settingsDscr;
        this.fxid = "#" + fxid;
    }

    /**
     * Gets the textual representation of the hot key key combination.
     * If this representation contains the 'Shortcut' modifier it will be replaced by the system specific modifier
     *
     * @return the representation
     */
    public String getShortcut() {
        return shortcut.replaceAll(KeyCode.SHORTCUT.getName(), SYSTEM_SPECIFIC_SHORTCUT_TEXT);
    }

    /**
     * @return the id used in the settings framework to save this hotkey
     */
    public String getSettingsId() {
        return settingsid;
    }

    /**
     * @return the description used in the settings framework
     */
    public String getSettingsDescription() {
        return settingsDscr;
    }

    /**
     * @return the if used in the fxml definition
     */
    public String getFxId() {
        return fxid;
    }

    /**
     * Set the shortcut text.
     *
     * @param shortcut the pattern
     */
    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }
}