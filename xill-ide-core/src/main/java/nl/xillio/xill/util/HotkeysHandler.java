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

import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import nl.xillio.xill.util.settings.Settings;
import nl.xillio.xill.util.settings.SettingsHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of hot-keys handler
 * It encapsulates everything that is needed for handling with hot-keys
 *
 * @author Zbynek Hochmann
 */
public class HotkeysHandler {
    /**
     * List of symbolic names of all hot-keys
     */
    public enum Hotkeys {
        @SuppressWarnings("javadoc")
        NEW,
        @SuppressWarnings("javadoc")
        OPEN,
        @SuppressWarnings("javadoc")
        SAVE,
        @SuppressWarnings("javadoc")
        SAVEAS,
        @SuppressWarnings("javadoc")
        SAVEALL,
        @SuppressWarnings("javadoc")
        CLOSE,
        @SuppressWarnings("javadoc")
        HELP,
        @SuppressWarnings("javadoc")
        RUN,
        @SuppressWarnings("javadoc")
        PAUSE,
        @SuppressWarnings("javadoc")
        STOP,
        @SuppressWarnings("javadoc")
        STEPIN,
        @SuppressWarnings("javadoc")
        STEPOVER,
        @SuppressWarnings("javadoc")
        CLEARCONSOLE,
        @SuppressWarnings("javadoc")
        COPY,
        @SuppressWarnings("javadoc")
        CUT,
        @SuppressWarnings("javadoc")
        PASTE,
        @SuppressWarnings("javadoc")
        RESET_ZOOM,
        @SuppressWarnings("javadoc")
        FIND,
        @SuppressWarnings("javadoc")
        OPENSETTINGS,
        @SuppressWarnings("javadoc")
        DUPLICATELINES,
        @SuppressWarnings("javadoc")
        RENAME,
        @SuppressWarnings("javadoc")
        ESCAPE
    }

    private HashMap<Hotkeys, Hotkey> hotkeys = new HashMap<>();

    /**
     * Constructor for HotkeysHandler
     * It does initialize internal the map of hotkeys and set all needed values
     */
    public HotkeysHandler() {
        init();
    }

    private void init() {
        // Shortcut is Ctrl on Windows and Meta on Mac.
        hotkeys.put(Hotkeys.ESCAPE, new Hotkey("Esc", Settings.Escape, "Shortcut to escape back to the editor.", "tfrename"));
        hotkeys.put(Hotkeys.NEW, new Hotkey("Shortcut+N", Settings.NewFile, "Shortcut to New file", "tfnewfile"));
        hotkeys.put(Hotkeys.OPEN, new Hotkey("Shortcut+O", Settings.OpenFile, "Shortcut to Open file", "tfopenfile"));
        hotkeys.put(Hotkeys.SAVE, new Hotkey("Shortcut+S", Settings.SaveFile, "Shortcut to Save file", "tfsavefile"));
        hotkeys.put(Hotkeys.SAVEAS, new Hotkey("Shortcut+Alt+S", Settings.SaveFileAs, "Shortcut to Save file as", "tfsavefileas"));
        hotkeys.put(Hotkeys.SAVEALL, new Hotkey("Shortcut+Shift+S", Settings.SaveAll, "Shortcut to Save all", "tfsaveall"));
        hotkeys.put(Hotkeys.CLOSE, new Hotkey("Shortcut+W", Settings.Close, "Shortcut to close currently open bot", "tfclose"));
        hotkeys.put(Hotkeys.HELP, new Hotkey("F1", Settings.HelpHome, "Shortcut to show help", "tfhelphome"));
        hotkeys.put(Hotkeys.RUN, new Hotkey("F6", Settings.Run, "Shortcut to run bot", "tfrun"));
        hotkeys.put(Hotkeys.PAUSE, new Hotkey("F7", Settings.Pause, "Shortcut to pause bot", "tfpause"));
        hotkeys.put(Hotkeys.STOP, new Hotkey("F8", Settings.Stop, "Shortcut to stop bot", "tfstop"));
        hotkeys.put(Hotkeys.STEPIN, new Hotkey("F9", Settings.Stepin, "Shortcut to step in", "tfstepin"));
        hotkeys.put(Hotkeys.STEPOVER, new Hotkey("F10", Settings.Stepover, "Shortcut to step over", "tfstepover"));
        hotkeys.put(Hotkeys.CLEARCONSOLE, new Hotkey("Shortcut+L", Settings.ClearConsole, "Shortcut to clear console", "tfclearconsole"));
        hotkeys.put(Hotkeys.COPY, new Hotkey("Shortcut+C", Settings.Copy, "Shortcut to copy to clipboard", "tfcopy"));
        hotkeys.put(Hotkeys.CUT, new Hotkey("Shortcut+X", Settings.Cut, "Shortcut to cut to clipboard", "tfcut"));
        hotkeys.put(Hotkeys.PASTE, new Hotkey("Shortcut+V", Settings.Paste, "Shortcut to paste from clipboard", "tfpaste"));
        hotkeys.put(Hotkeys.RESET_ZOOM, new Hotkey("Shortcut+0", Settings.ResetZoom, "Shortcut to reset zoom", "tfresetzoom"));
        hotkeys.put(Hotkeys.FIND, new Hotkey("Shortcut+F", Settings.Search, "Shortcut to start search", "tfsearch"));
        hotkeys.put(Hotkeys.OPENSETTINGS, new Hotkey("Shortcut+P", Settings.OpenSettings, "Shortcut to open settings dialog", "tfopensettings"));
        hotkeys.put(Hotkeys.DUPLICATELINES, new Hotkey("Shortcut+D", Settings.DuplicateLines, "Shortcut to duplicate selected lines", "tfduplicatelines"));
        hotkeys.put(Hotkeys.RENAME, new Hotkey("F2", Settings.Rename, "Shortcut to rename the selected file/folder.", "tfrename"));
    }

    /**
     * Takes current settings of hot-keys and assign their key shortcuts
     *
     * @param settings SettingsHandler instance
     */
    public void setHotkeysFromSettings(final SettingsHandler settings) {
        hotkeys.entrySet().forEach(hk -> hk.getValue().setShortcut(settings.simple().get(Settings.SETTINGS_KEYBINDINGS, hk.getValue().getSettingsId())));
    }

    /**
     * Takes current settings of hot-keys and fills in all shortcut fields in SettingDialog
     *
     * @param scene    Scene instance of SettingsDialog
     * @param settings SettingsHandler instance
     */
    public void setDialogFromSettings(final Scene scene, final SettingsHandler settings) {
        hotkeys.entrySet().forEach(hk -> {
            TextField tf = (TextField) scene.lookup(hk.getValue().getFxId());
            tf.setText(settings.simple().get(Settings.SETTINGS_KEYBINDINGS, hk.getValue().getSettingsId()));
        });
    }

    /**
     * Try to find provided shortcut in all shortcut fields in SettingDialog
     *
     * @param scene    Scene instance of SettingsDialog
     * @param shortcut string interpretation of shortcut
     * @return TextField that contains provided shortcut, if not found it returns null
     */
    public TextField findShortcutInDialog(final Scene scene, final String shortcut) {
        return hotkeys.entrySet().stream()
                .map(hk -> (TextField) scene.lookup(hk.getValue().getFxId()))
                .filter(tf -> tf.getText().equals(shortcut))
                .findAny()
                .orElse(null);
    }

    /**
     * Iterate shortcut fields in SettingDialog
     *
     * @param scene Scene instance of SettingsDialog
     * @return list of all shortcut fields in SettingDialog
     */
    public List<TextField> getAllTextFields(final Scene scene) {
        return hotkeys.entrySet().stream()
                .map(entry -> (TextField) scene.lookup(entry.getValue().getFxId()))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Takes all current shortcut settings from SettingsDialog and store them into settings
     *
     * @param scene    Scene instance of SettingsDialog
     * @param settings SettingsHandler instance
     */
    public void saveSettingsFromDialog(final Scene scene, final SettingsHandler settings) {
        hotkeys.entrySet().stream().forEach(hk -> settings.simple().save(Settings.SETTINGS_KEYBINDINGS, hk.getValue().getSettingsId(), ((TextField) scene.lookup(hk.getValue().getFxId())).getText()));
    }

    /**
     * @param hk symbolic hot-key
     * @return string representation of provided hot-key
     */
    public String getShortcut(final Hotkeys hk) {
        return hotkeys.get(hk).getShortcut();
    }

    /**
     * @param keyEvent key combination
     * @return symbolic hot-key from provided key combination, return null if not found
     */
    public Hotkeys getHotkey(final KeyEvent keyEvent) {
        return hotkeys.entrySet().stream()
                .filter(o -> (KeyCombination.valueOf(o.getValue().getShortcut()).match(keyEvent)))
                .map(Map.Entry::getKey)
                .findAny()
                .orElse(null);
    }

    /**
     * Register all hot-key settings
     *
     * @param settings SettingsHandler instance
     */
    public void registerHotkeysSettings(final SettingsHandler settings) {
        hotkeys.entrySet().forEach(hk -> settings.simple().register(Settings.SETTINGS_KEYBINDINGS, hk.getValue().getSettingsId(), hk.getValue().getShortcut(), hk.getValue().getSettingsDescription()));
    }
}
