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
    public static final String LeftPanelWidth = "LeftPanelWidth";

    /**
     * The collapsed-state of the left panel
     */
    public static final String LeftPanelCollapsed = "LeftPanelCollapsed";

    /**
     * The height of the project panel
     */
    public static final String ProjectHeight = "ProjectHeight";

    /**
     * Width of the right panel for the specified currentRobot
     */
    public static final String RightPanelWidth_ = "RightPanelWidth_";

    /**
     * The collapsed-state of the right panel for the specified currentRobot
     */
    public static final String RightPanelCollapsed_ = "RightPanelCollapsed_";

    /**
     * The height of the editor
     */
    public static final String EditorHeight_ = "EditorHeight_";

    /**
     * The height of the preview panel
     */
    public static final String PreviewHeight_ = "PreviewHeight_";

    /**
     * The zoom factor of the code editor.
     */
    public static final String AceZoom_ = "AceZoom_";

    /**
     * The dimensions of the settings dialog
     */
    public static final String SettingsDialogDimensions = "SettingsDialogDimensions";

    // =============================================================================
    /**
     * File category
     */
    public static final String FILE = "File";

    /**
     * The last folder a file was opened from or saved to
     */
    public static final String LastFolder = "LastFolder";

    // =============================================================================
    /**
     * Warning category
     */
    public static final String WARNING = "Warning";

    /**
     * Show warning dialogs for debug messages
     */
    public static final String DialogDebug = "DialogDebug";

    /**
     * Show warning dialogs for info messages
     */
    public static final String DialogInfo = "DialogInfo";

    /**
     * Show warning dialogs for warning messages
     */
    public static final String DialogWarning = "DialogWarning";

    /**
     * Show warning dialogs for error messages
     */
    public static final String DialogError = "DialogError";

    // =============================================================================
    /**
     * Server category
     */
    public static final String SERVER = "Server";

    /**
     * Optional username to access XMTS
     */
    public static final String ServerUsername = "ServerUsername";

    /**
     * Optional password to access XMTS
     */
    @SuppressWarnings("squid:S2068") // Credentials should not be hard-coded.
    public static final String ServerPassword = "ServerPassword";

    /**
     * Location XMTS is running on
     */
    public static final String ServerHost = "ServerHost";

    /**
     * Address of Xill server
     */
    public static final String XillServerHost = "XillServerHost";

    /**
     * Username for accessing the Xill server
     */
    public static final String XillServerUsername = "XillServerUsername";

    /**
     * Password for accessing the Xill server
     */
    public static final String XillServerPassword = "XillServerPassword";

    // =============================================================================
    /**
     * Info category
     */
    public static final String INFO = "Info";

    /**
     * Last version that was run
     */
    public static final String LastVersion = "LastVersion";

    /**
     * Whether the IDE has run before
     */
    public static final String HasRun = "HasRun";

    /**
     * Whether the user accepted the EULA
     */
    public static final String EulaAccepted = "EulaAccepted";

    // =============================================================================
    /**
     * Workspace category
     */
    public static final String WORKSPACE = "Workspace";

    /**
     * List of last time open tabs
     */
    public static final String OpenTabs = "OpenTabs";

    /**
     * Last time active tab
     */
    public static final String ActiveTab = "ActiveTab";

    // =============================================================================
    /**
     * General settings dialog
     */
    public static final String SETTINGS_GENERAL = "SettingsGeneral";

    /** */
    public static final String DefaultProjectLocation = "DefaultProjectLocation";

    /** */
    public static final String OpenBotWithCleanConsole = "OpenBotWithCleanConsole";

    /** */
    public static final String RunBotWithCleanConsole = "RunBotWithCleanConsole";

    /** */
    public static final String EnableAutoSave = "EnableAutoSave";

    // =============================================================================
    /**
     * Editor settings dialog
     */
    public static final String SETTINGS_EDITOR = "SettingsEditor";

    /** */
    public static final String DisplayIndentGuides = "DisplayIndentGuides";

    /** */
    public static final String FontSize = "FontSize";

    /** */
    public static final String AutoSaveBotBeforeRun = "AutoSaveBotBeforeRun";

    /** */
    public static final String HighlightSelectedWord = "HighlightSelectedWord";

    /** */
    public static final String NewLineMode = "NewLineMode";

    /** */
    public static final String PrintMarginColumn = "PrintMarginColumn";

    /** */
    public static final String ShowGutter = "ShowGutter";

    /** */
    public static final String ShowInvisibles = "ShowInvisibles";

    /** */
    public static final String TabSize = "TabSize";

    /** */
    public static final String UseSoftTabs = "UseSoftTabs";

    /** */
    public static final String WrapText = "WrapText";

    /** */
    public static final String WrapLimit = "WrapLimit";

    /** */
    public static final String ShowPrintMargin = "ShowPrintMargin";

    /** */
    public static final String ShowLineNumbers = "ShowLineNumbers";

    /** */
    // =============================================================================
    /**
     * Hot-keys settings
     */
    public static final String SETTINGS_KEYBINDINGS = "KeyBindings";

    /** */
    public static final String NewFile = "NewFile";

    /** */
    public static final String OpenFile = "OpenFile";

    /** */
    public static final String SaveFile = "SaveFile";

    /** */
    public static final String SaveFileAs = "SaveFileAs";

    /** */
    public static final String SaveAll = "SaveFileAll";

    /** */
    public static final String Close = "Close";

    /** */
    public static final String HelpHome = "HelpHome";

    /** */
    public static final String Run = "Run";

    /** */
    public static final String Pause = "Pause";

    /** */
    public static final String Stop = "Stop";

    /** */
    public static final String Stepin = "Stepin";

    /** */
    public static final String Stepover = "Stepover";

    /** */
    public static final String ClearConsole = "ClearConsole";

    /** */
    public static final String Search = "Search";

    /** */
    public static final String ResetZoom = "ResetZoom";

    /** */
    public static final String Copy = "Copy";

    /** */
    public static final String Cut = "Cut";

    /** */
    public static final String Paste = "Paste";

    /** */
    public static final String DuplicateLines = "DuplicateLines";

    /** */
    public static final String OpenSettings = "OpenSettings";

    /** */
    public static final String Rename = "Rename";

    /** */
    public static final String Escape = "Escape";
    // =============================================================================
}
