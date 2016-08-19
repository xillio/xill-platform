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
 * Just list of categories and simple variable names
 *
 * @author Zbynek Hochmann
 */
public class Settings {

    // =============================================================================
    /**
     * Layout category
     */
    public static final String LAYOUT = "Layout";

    /**
     * Width of the left panel
     */
    public static final String LEFT_PANEL_WIDTH = "LeftPanelWidth";

    /**
     * The collapsed-state of the left panel
     */
    public static final String LEFT_PANEL_COLLAPSED = "LeftPanelCollapsed";

    /**
     * The height of the project panel
     */
    public static final String PROJECT_HEIGHT = "ProjectHeight";

    /**
     * Width of the right panel for the specified currentRobot
     */
    public static final String RIGHT_PANEL_WIDTH = "RightPanelWidth_";

    /**
     * The collapsed-state of the right panel for the specified currentRobot
     */
    public static final String RIGHT_PANEL_COLLAPSED = "RightPanelCollapsed_";

    /**
     * The height of the editor
     */
    public static final String EDITOR_HEIGHT = "EditorHeight_";

    /**
     * The height of the preview panel
     */
    public static final String PREVIEW_HEIGHT = "PreviewHeight_";

    /**
     * The dimensions of the settings dialog
     */
    public static final String SETTINGS_DIALOG_DIMENSIONS = "SettingsDialogDimensions";

    // =============================================================================
    /**
     * File category
     */
    public static final String FILE = "File";

    /**
     * The last folder a file was opened from or saved to
     */
    public static final String LAST_FOLDER = "LastFolder";

    /**
     * Show non-Xill files
     */
    public static final String SHOW_ALL_FILES = "ShowAllFiles";

    // =============================================================================
    /**
     * Warning category
     */
    public static final String WARNING = "Warning";

    /**
     * Show warning dialogs for debug messages
     */
    public static final String DIALOG_DEBUG = "DialogDebug";

    /**
     * Show warning dialogs for info messages
     */
    public static final String DIALOG_INFO = "DialogInfo";

    /**
     * Show warning dialogs for warning messages
     */
    public static final String DIALOG_WARNING = "DialogWarning";

    /**
     * Show warning dialogs for error messages
     */
    public static final String DIALOG_ERROR = "DialogError";

    // =============================================================================
    /**
     * Server category
     */
    public static final String SERVER = "Server";

    /**
     * Address of Xill server
     */
    public static final String XILL_SERVER_HOST = "XillServerHost";

    /**
     * Username for accessing the Xill server
     */
    public static final String XILL_SERVER_USERNAME = "XillServerUsername";

    /**
     * Password for accessing the Xill server
     */
    @SuppressWarnings("squid:S2068") // This is just a key, not an actual password
    public static final String XILL_SERVER_PASSWORD = "XillServerPassword";

    // =============================================================================
    /**
     * Info category
     */
    public static final String INFO = "Info";

    /**
     * Last version that was run
     */
    public static final String LAST_VERSION = "LastVersion";

    /**
     * Whether the IDE has run before
     */
    public static final String HAS_RUN = "HasRun";

    /**
     * Whether the user accepted the EULA
     */
    public static final String EULA_ACCEPTED = "EulaAccepted";

    // =============================================================================
    /**
     * Workspace category
     */
    public static final String WORKSPACE = "Workspace";

    /**
     * List of last time open tabs
     */
    public static final String OPEN_TABS = "OpenTabs";

    /**
     * Last time active tab
     */
    public static final String ACTIVE_TAB = "ActiveTab";

    // =============================================================================
    /**
     * General settings dialog
     */
    public static final String SETTINGS_GENERAL = "SettingsGeneral";

    /** */
    public static final String DEFAULT_PROJECT_LOCATION = "DefaultProjectLocation";

    /** */
    public static final String OPEN_BOT_WITH_CLEAN_CONSOLE = "OpenBotWithCleanConsole";

    /** */
    public static final String RUN_BOT_WITH_CLEAN_CONSOLE = "RunBotWithCleanConsole";

    /** */
    public static final String ENABLE_AUTO_SAVE = "EnableAutoSave";

    // =============================================================================
    /**
     * Editor settings dialog
     */
    public static final String SETTINGS_EDITOR = "SettingsEditor";

    /** */
    public static final String DISPLAY_INDENT_GUIDES = "DisplayIndentGuides";

    /** */
    public static final String FONT_SIZE = "FontSize";

    /** */
    public static final String AUTO_SAVE_BOT_BEFORE_RUN = "AutoSaveBotBeforeRun";

    /** */
    public static final String HIGHLIGHT_SELECTED_WORD = "HighlightSelectedWord";

    /** */
    public static final String NEW_LINE_MODE = "NewLineMode";

    /** */
    public static final String PRINT_MARGIN_COLUMN = "PrintMarginColumn";

    /** */
    public static final String PRINTER_FONT_SIZE = "PrinterFontSize";

    /** */
    public static final String SHOW_GUTTER = "ShowGutter";

    /** */
    public static final String SHOW_INVISIBLES = "ShowInvisibles";

    /** */
    public static final String TAB_SIZE = "TabSize";

    /** */
    public static final String USE_SOFT_TABS = "UseSoftTabs";

    /** */
    public static final String WRAP_TEXT = "WrapText";

    /** */
    public static final String WRAP_LIMIT = "WrapLimit";

    /** */
    public static final String SHOW_PRINT_MARGIN = "ShowPrintMargin";

    /** */
    public static final String SHOW_LINE_NUMBERS = "ShowLineNumbers";

    /** */
    // =============================================================================
    /**
     * Hot-keys settings
     */
    public static final String SETTINGS_KEYBINDINGS = "KeyBindings";

    /** */
    public static final String NEW_FILE = "NewFile";

    /** */
    public static final String OPEN_FILE = "OpenFile";

    /** */
    public static final String SAVE_FILE = "SaveFile";

    /** */
    public static final String SAVE_FILE_AS = "SaveFileAs";

    /** */
    public static final String SAVE_FILE_ALL = "SaveFileAll";

    /** */
    public static final String CLOSE = "Close";

    /** */
    public static final String HELP_HOME = "HelpHome";

    /** */
    public static final String RUN = "Run";

    /** */
    public static final String PAUSE = "Pause";

    /** */
    public static final String STOP = "Stop";

    /** */
    public static final String STEP_IN = "Stepin";

    /** */
    public static final String STEP_OVER = "Stepover";

    /** */
    public static final String CLEAR_CONSOLE = "ClearConsole";

    /** */
    public static final String SEARCH = "Search";

    /** */
    public static final String RESET_ZOOM = "ResetZoom";

    /** */
    public static final String COPY = "Copy";

    /** */
    public static final String CUT = "Cut";

    /** */
    public static final String PASTE = "Paste";

    /** */
    public static final String DUPLICATE_LINES = "DuplicateLines";

    /** */
    public static final String OPEN_SETTINGS = "OpenSettings";

    /** */
    public static final String RENAME = "Rename";

    /** */
    public static final String ESCAPE = "Escape";

    // =============================================================================

    private Settings() {
    }
}
