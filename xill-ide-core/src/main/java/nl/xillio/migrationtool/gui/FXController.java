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
package nl.xillio.migrationtool.gui;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import me.biesaart.utils.Log;
import nl.xillio.events.EventHost;
import nl.xillio.migrationtool.ApplicationKillThread;
import nl.xillio.migrationtool.EulaUtils;
import nl.xillio.migrationtool.Loader;
import nl.xillio.migrationtool.dialogs.AlertDialog;
import nl.xillio.migrationtool.dialogs.ChangeLogDialog;
import nl.xillio.migrationtool.dialogs.MissingLicensePluginsDialog;
import nl.xillio.migrationtool.dialogs.SettingsDialog;
import nl.xillio.migrationtool.elasticconsole.ESConsoleClient;
import nl.xillio.plugins.PluginLoadFailure;
import nl.xillio.plugins.XillPlugin;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.util.HotkeysHandler;
import nl.xillio.xill.util.HotkeysHandler.Hotkeys;
import nl.xillio.xill.util.settings.Settings;
import nl.xillio.xill.util.settings.SettingsHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.pegdown.PegDownProcessor;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * This class is the global controller for the application
 */
public class FXController implements Initializable, EventHandler<Event> {

    /**
     * Instance of settings handler
     */
    public static final SettingsHandler settings = SettingsHandler.getSettingsHandler();
    public static final EventHost<String> OPEN_ROBOT_EVENT = new EventHost<>();

    /**
     * Instance of hotkeys handler
     */
    public static final HotkeysHandler hotkeys = new HotkeysHandler();

    private static final File DEFAULT_OPEN_BOT = new File("samples/Hello-Xillio" + XillEnvironment.ROBOT_EXTENSION);
    private static final Logger LOGGER = Log.get();
    private boolean cancelClose = false; // should be the closing of application interrupted?
    @FXML
    private Button btnNewFile;
    @FXML
    private Button btnOpenFile;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnSaveAs;
    @FXML
    private Button btnSaveAll;
    @FXML
    private Button btnPrint;
    @FXML
    private Button btnSettings;
    @FXML
    private Button btnRemoveAllBreakpoints;
    @FXML
    private Button btnEvaluate;
    @FXML
    private Button btnRun;
    @FXML
    private Button btnStepOver;
    @FXML
    private Button btnStepIn;
    @FXML
    private Button btnPause;
    @FXML
    private Button btnStop;
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnBrowser;
    @FXML
    private Button btnRegexTester;
    @FXML
    private Button btnPreviewOpenBrowser;
    @FXML
    private Button btnPreviewOpenRegex;
    @FXML
    private TabPane tpnBots;
    @FXML
    private AnchorPane apnRoot;
    @FXML
    private AnchorPane apnLeft;
    @FXML
    private VBox vbxLeftHidden;
    @FXML
    private HBox hbxMain;
    @FXML
    private SplitPane spnMain;
    @FXML
    private SplitPane spnLeft;
    @FXML
    private ProjectPane projectpane;
    @FXML
    private HelpPane helppane;

    private ReturnFocusListener returnFocusListener;
    private SettingsDialog settingsDialog;

    /**
     * Initialize custom components
     */
    @Override
    public void initialize(final URL url, final ResourceBundle bundle) {

        // Register most of the internal settings
        registerSettings();

        // Set hotkeys from settings
        hotkeys.setHotkeysFromSettings(settings);

        // Initialize layout and layout listeners
        Platform.runLater(() -> {
            // Add splitpane position listener
            spnMain.getDividers().get(0).positionProperty().addListener((observable, oldPos, newPos) -> {
                if (spnMain.getItems().contains(apnLeft)) {
                    settings.simple().save(Settings.LAYOUT, Settings.LEFT_PANEL_WIDTH, newPos.toString());
                }
            });

            spnMain.setDividerPosition(0, Double.parseDouble(settings.simple().get(Settings.LAYOUT, Settings.LEFT_PANEL_WIDTH)));
            // Remove the left hidden bar from dom
            if (Boolean.parseBoolean(settings.simple().get(Settings.LAYOUT, Settings.LEFT_PANEL_COLLAPSED))) {
                btnHideLeftPane();
            } else {
                btnShowLeftPane();
            }

            spnLeft.setDividerPosition(0, Double.parseDouble(settings.simple().get(Settings.LAYOUT, Settings.PROJECT_HEIGHT)));
            spnLeft.getDividers().get(0).positionProperty().addListener((observable, oldPos, newPos) -> settings
                    .simple().save(Settings.LAYOUT, Settings.PROJECT_HEIGHT, Double.toString(newPos.doubleValue())));
        });

        // Start the elasticsearch console
        Platform.runLater(ESConsoleClient::getInstance);

        tpnBots.getTabs().clear();
        projectpane.setGlobalController(this);

        // Hide left collapsed panel from the dom at startup
        hbxMain.getChildren().remove(vbxLeftHidden);

        // Add window handler
        Platform.runLater(() -> apnRoot.getScene().getWindow().setOnCloseRequest(event -> {
            this.cancelClose = false;
            LOGGER.info("Shutting down application.");
            if (!closeApplication()) {
                event.consume(); // this cancel the process of the application closing
            }
        }));

        Platform.runLater(() -> apnRoot.getScene().getWindow().focusedProperty().addListener((event, oldValue, newValue) -> {
                    if (newValue) {
                        getTabs().forEach(e -> projectpane.fileChanged(e.getDocument()));
                    }
                })
        );

        apnRoot.addEventFilter(KeyEvent.KEY_PRESSED, this);

        // Add listener for window shown
        loadWorkSpace();

        Platform.runLater(() -> {
            returnFocusListener = new ReturnFocusListener(apnRoot.getScene());
            buttonsReturnFocus(apnRoot);
        });

        if (projectpane.getProjectsCount() == 0) {
            btnNewFile.setDisable(true);
            btnOpenFile.setDisable(true);
        }
        if (getTabs().isEmpty()) {
            disableSaveButtons(true);
        }

    }

    private void createSettingsWindow() {
        settingsDialog = new SettingsDialog(settings);
        settingsDialog.setOnApply(() -> {
            // Apply all settings immediately
            hotkeys.setHotkeysFromSettings(settings); // Apply new hotkeys settings
            getTabs().forEach(tab -> tab.getEditorPane().setEditorOptions(createEditorOptionsJSCode())); // Apply editor settings
        });
    }

    private void registerSettings() {
        settings.setManualCommit(true);

        settings.simple().register(Settings.FILE, Settings.LAST_FOLDER, System.getProperty("user.dir"), "The last folder a file was opened from or saved to.");
        settings.simple().register(Settings.WARNING, Settings.DIALOG_DEBUG, "false", "Show warning dialogs for debug messages.");
        settings.simple().register(Settings.WARNING, Settings.DIALOG_INFO, "false", "Show warning dialogs for info messages.");
        settings.simple().register(Settings.WARNING, Settings.DIALOG_WARNING, "false", "Show warning dialogs for warning messages.");
        settings.simple().register(Settings.WARNING, Settings.DIALOG_ERROR, "true", "Show warning dialogs for error messages.");
        settings.simple().register(Settings.SERVER, Settings.XILL_SERVER_HOST, "http://localhost:8080", "Location where Xill server is running.");
        settings.simple().register(Settings.SERVER, Settings.XILL_SERVER_USERNAME, "", "Username to access Xill server.", true);
        settings.simple().register(Settings.SERVER, Settings.XILL_SERVER_PASSWORD, "", "Password to access Xill server.", true);
        settings.simple().register(Settings.INFO, Settings.LAST_VERSION, "0.0.0", "Last version that was run.");
        settings.simple().register(Settings.LAYOUT, Settings.LEFT_PANEL_WIDTH, "0.2", "Width of the left panel");
        settings.simple().register(Settings.LAYOUT, Settings.LEFT_PANEL_COLLAPSED, "false", "The collapsed-state of the left panel");
        settings.simple().register(Settings.LAYOUT, Settings.PROJECT_HEIGHT, "0.5", "The height of the project panel");

        SettingsDialog.register(settings);

        settings.commit();
        settings.setManualCommit(false);
    }

    private void loadWorkSpace() {
        Platform.runLater(() -> {
            String workspace = settings.simple().get(Settings.WORKSPACE, Settings.OPEN_TABS);
            // Wait for all plugins to be loaded before loading the workspace.
            Loader.getInitializer().getPlugins();

            if (workspace == null) {
                workspace = DEFAULT_OPEN_BOT.getAbsolutePath();
            }

            if (!"".equals(workspace)) {
                String[] files = workspace.split(";");
                for (final String filename : files) {
                    openFile(new File(filename));
                }
            }
        });

        OPEN_ROBOT_EVENT.getEvent().addListener(
                bot -> Platform.runLater(() -> {
                    openFile(new File(bot));
                    tpnBots.getSelectionModel().selectLast();
                })
        );

        Platform.runLater(() -> {
            // Verify the license.
            if (!EulaUtils.performEulaCheck(false)) {
                closeApplication();
            }

            try {
                showReleaseNotes();
            } catch (IOException e) {
                LOGGER.error("Failed to show release notes: " + e.getMessage(), e);
            }

            showMissingLicensePlugins();

            // Select the last opened tab.
            String activeTab = settings.simple().get(Settings.WORKSPACE, Settings.ACTIVE_TAB);
            if (activeTab != null && !"".equals(activeTab)) {
                getTabs().stream()
                        .filter(tab -> tab.getDocument().getAbsolutePath().equals(activeTab))
                        .forEach(tab -> tpnBots.getSelectionModel().select(tab));
            }
        });
    }

    /**
     * Create a new robot file.
     */
    @FXML
    private void buttonNewFile() {
        if (!btnNewFile.isDisabled()) {
            projectpane.newBot(null);
        }
    }

    /**
     * Switch focus to an existing tab for a certain file or create it.
     *
     * @param document the file that should be visible in the tab
     * @param project  the project folder for the robot
     * @param isRobot  whether the file is a robot or regular file
     */
    public void viewOrOpenRobot(File document, File project, boolean isRobot) {
        FileTab tab = findTab(document);

        // Create a tab.
        if (tab == null) {
            tab = createTab(document, project, isRobot);
        }

        // Open the tab.
        if (tab != null) {
            tab.requestFocus();
        }
    }

    /**
     * Try to make a tab and add it to the view.
     *
     * @param document the file that should be visible in the tab
     * @param project  the project folder for the robot
     * @return the tab or null if it could not be created
     */
    private FileTab createTab(File document, File project, boolean isRobot) {
        FileTab tab;
        if (isRobot) {
            tab = new RobotTab(project, document, this);
        } else {
            //check white list
            if (!isWhiteListed(document.getName())) {
                AlertDialog dialog = new AlertDialog(Alert.AlertType.WARNING,
                        "Unsupported file type",
                        "The file '" + document.getName() + "' has an unsupported type.",
                                "The editing of non-text-files is ill advised and may corrupt the file." + System.lineSeparator() +
                        "Do you want to continue?",
                        ButtonType.YES, ButtonType.NO);
                final Optional<ButtonType> result = dialog.showAndWait();
                if (!result.isPresent() || result.get() == ButtonType.NO) {
                    return null;
                }
            }
            tab = new FileTab(project, document, this);
        }

        tpnBots.getTabs().add(tab);
        return tab;
    }

    private boolean isWhiteListed(String fileName) {
        List<String> whiteList = Arrays.asList("xill", "txt", "properties", "html", "htm", "css", "xslt", "xml", "json", "js", "md", "cfg", "ini", "bat", "sh", "sbot");
        return whiteList.contains(FilenameUtils.getExtension(fileName));
    }

    @FXML
    private void buttonOpenFile() {
        // Check if the button is enabled.
        if (btnOpenFile.isDisabled()) {
            return;
        }

        // If the last picked folder exists, set the initial directory to that.
        FileChooser fileChooser = new FileChooser();
        File lastFolder = new File(settings.simple().get(Settings.FILE, Settings.LAST_FOLDER));
        if (lastFolder.exists()) {
            fileChooser.setInitialDirectory(lastFolder);
        }

        // Show the dialog and open the file.
        String robotExtension = "*" + XillEnvironment.ROBOT_EXTENSION;
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Xillio scripts (" + robotExtension + ")", robotExtension),
                new FileChooser.ExtensionFilter("All files (*.*)", "*.*")
        );
        File newFile = fileChooser.showOpenDialog(btnOpenFile.getScene().getWindow());

        if (newFile != null) {
            openFile(newFile);
        }
    }

    /**
     * Open a file
     *
     * @param file the file to open
     * @return the tab that was opened or null if something went wrong
     */
    public FileTab openFile(final File file) {
        return doOpenFile(file, file.toString().endsWith(XillEnvironment.ROBOT_EXTENSION));
    }

    /**
     * Open a robot
     *
     * @param file the file to open
     * @return the tab that was opened or null if something went wrong
     */
    public RobotTab openRobot(final File file) {
        return (RobotTab) doOpenFile(file, true);
    }

    private FileTab doOpenFile(final File file, boolean isRobot) {
        // Skip if the file doesn't exist
        if (!file.exists() || !file.isFile()) {
            LOGGER.error("Failed to open file `" + file.getAbsolutePath() + "`. File not found.");
            return null;
        }

        // Verify file isn't open already
        for (Tab tab : tpnBots.getTabs()) {
            FileTab editor = (FileTab) tab;
            try {
                if (editor.getDocument() != null && editor.getDocument().getCanonicalPath().equals(file.getCanonicalPath())) {
                    tpnBots.getSelectionModel().select(editor);

                    showTab(editor);
                    editor.requestFocus();

                    return editor;
                }
            } catch (IOException e) {
                LOGGER.error("Error while opening file: " + e.getMessage(), e);
                return null;
            }
        }

        // Try to get the project path. If there is no project, use the parent directory as the project path.
        String projectPath = projectpane.getProjectPath(file).orElse(file.getParent());

        //try to create the new tab
        FileTab tab = createTab(file.getAbsoluteFile(), new File(projectPath), isRobot);

        if (tab == null) {
            return null;
        }

        // Tab is not open yet: open new tab
        settings.simple().save(Settings.FILE, Settings.LAST_FOLDER, file.getParent());

        //open the new tab
        tab.requestFocus();
        return tab;
    }

    /**
     * Make all buttons that are children of the given node return focus to the previously focused node after receiving focus
     *
     * @param node The node to search for buttons in
     */
    protected void buttonsReturnFocus(Node node) {
        // For some nodes (like SplitPane) children are added by the skin
        // Apply CSS to make sure all children are present
        node.applyCss();

        node.lookupAll(".button")
                .forEach(n ->
                        n.focusedProperty().addListener(
                                returnFocusListener));

        // MenuButtons do not seem to get children, only items. Handle them as a special case.
        node.lookupAll(".menu-button").forEach(mb -> {
            if (mb instanceof MenuButton) {
                ((MenuButton) mb).showingProperty().not().addListener(returnFocusListener);
            }
        });
        node.lookupAll(".toggle-button").forEach(tb -> tb.focusedProperty().addListener(returnFocusListener));

        //maybe need to add more here... since some things do not yet return focus.
    }

    @FXML
    private void buttonSave() {
        if (!btnSave.isDisabled()) {
            Tab tab = tpnBots.getSelectionModel().getSelectedItem();
            if (tab != null) {
                ((FileTab) tab).save();
            }
        }
    }

    @FXML
    private void buttonSaveAs() {
        if (!btnSaveAs.isDisabled()) {
            Tab tab = tpnBots.getSelectionModel().getSelectedItem();
            if (tab != null) {
                ((FileTab) tab).save(true);
            }
        }
    }

    @FXML
    private void buttonSaveAll() {
        if (!btnSaveAll.isDisabled()) {
            tpnBots.getTabs().forEach(tab -> ((FileTab) tab).save());
        }
    }

    /*
    Temporarily disabled print button until functionality is complete,
    see: https://xillio.atlassian.net/browse/CTC-1752
    
    @FXML
    private void buttonPrint(){
        Tab tab = tpnBots.getSelectionModel().getSelectedItem();
        if( tab != null){
            ((FileTab) tab).getEditorPane().print();
        }
    }*/

    @FXML
    private void buttonSettings() {
        if (!btnSettings.isDisabled()) {
            createSettingsWindow();
            settingsDialog.show();
        }
    }

    @FXML
    private void btnHideLeftPane() {
        settings.simple().save(Settings.LAYOUT, Settings.LEFT_PANEL_WIDTH, "" + spnMain.getDividerPositions()[0]);
        settings.simple().save(Settings.LAYOUT, Settings.LEFT_PANEL_COLLAPSED, "true");
        spnMain.getItems().remove(apnLeft);
        if (!hbxMain.getChildren().contains(vbxLeftHidden)) {
            hbxMain.getChildren().add(0, vbxLeftHidden);
        }
    }

    @FXML
    private void btnShowLeftPane() {
        settings.simple().save(Settings.LAYOUT, Settings.LEFT_PANEL_COLLAPSED, "false");

        hbxMain.getChildren().remove(vbxLeftHidden);
        if (!spnMain.getItems().contains(apnLeft)) {
            spnMain.getItems().add(0, apnLeft);
            spnMain.setDividerPosition(0, Double.parseDouble(settings.simple().get(Settings.LAYOUT, Settings.LEFT_PANEL_WIDTH)));
        }

        FileTab selected = (FileTab) getSelectedTab();
        if (selected != null)
            selected.requestFocus();
    }

    private boolean closeApplication() {
        String openTabs = String.join(";", getTabs().stream().map(tab -> tab.getDocument().getAbsolutePath()).collect(Collectors.toList()));

        // Save all tabs
        settings.simple().save(Settings.WORKSPACE, Settings.OPEN_TABS, openTabs, true);

        // Save active tab
        final String[] activeTab = {null};
        getTabs().stream().filter(Tab::isSelected).forEach(tab -> activeTab[0] = tab.getDocument().getAbsolutePath());
        if (activeTab[0] != null) {
            settings.simple().save(Settings.WORKSPACE, Settings.ACTIVE_TAB, activeTab[0], true);
        } else {
            settings.simple().save(Settings.WORKSPACE, Settings.ACTIVE_TAB, "", true);
        }

        // Check if there are tabs whose robots are running.
        List<FileTab> running = getTabs().stream().filter(tab -> tab instanceof RobotTab)
                .filter(tab -> ((RobotTab) tab).getEditorPane().getControls().robotRunning()).collect(Collectors.toList());
        // If robots are running, show a confirmation dialog.
        if (!running.isEmpty() && !showCloseAppDialog(running.size())) {
            return false;
        }

        // Close all tabs
        tpnBots.getTabs().forEach(tab -> {
            if (!this.cancelClose) {
                closeTab(tab, false, true);
            }
        });
        if (this.cancelClose) {
            return false; // Cancel the application closing
        }

        // Purge plugins
        for (XillPlugin plugin : Loader.getInitializer().getPlugins()) {
            try {
                plugin.close();
            } catch (Exception e) {
                LOGGER.error("Error while closing plugin: " + e.getMessage(), e);
            }
        }

        // Finish app closing
        ProjectPane.stop();
        Platform.exit();
        ESConsoleClient.getInstance().close();
        ApplicationKillThread.exit();
        return true;
    }

    private boolean showCloseAppDialog(int running) {
        AlertDialog dialog = new AlertDialog(Alert.AlertType.WARNING, "One or more robots are still running.", "",
                "You are about to quit the application but " + running + " robot(s) are still running." +
                        "All running robots will be stopped when you quit. Do you want to quit the application?",
                new ButtonType("Quit", ButtonBar.ButtonData.YES), ButtonType.CANCEL);
        return dialog.showAndWait().get().getButtonData() == ButtonBar.ButtonData.YES;
    }

    private String formatEditorOptionJSRaw(final String optionJS, final String keyValue) {
        return String.format("%1$s: %2$s,%n", optionJS, settings.simple().get(Settings.SETTINGS_EDITOR, keyValue));
    }

    private String formatEditorOptionJS(final String optionJS, final String keyValue) {
        return String.format("%1$s: \"%2$s\",%n", optionJS, settings.simple().get(Settings.SETTINGS_EDITOR, keyValue));
    }

    private String formatEditorOptionJSBoolean(final String optionJS, final String keyValue) {
        return String.format("%1$s: %2$s,%n", optionJS, Boolean.toString(settings.simple().getBoolean(Settings.SETTINGS_EDITOR, keyValue)));
    }

    /**
     * Creates JavaScript code that sets Ace editor's options according to current settings
     *
     * @return JavaScript code
     */
    public String createEditorOptionsJSCode() {
        String jsCode = "var editor = javaEditor.getAce();\neditor.setOptions({\n";
        String jsSettings = "";

        String s = settings.simple().get(Settings.SETTINGS_EDITOR, Settings.FONT_SIZE);
        if (s.endsWith("px")) {
            s = s.substring(0, s.length() - 2);
        }
        jsSettings += String.format("fontSize: \"%1$spt\",%n", s);

        jsSettings += formatEditorOptionJSBoolean("displayIndentGuides", Settings.DISPLAY_INDENT_GUIDES);
        jsSettings += formatEditorOptionJS("newLineMode", Settings.NEW_LINE_MODE);
        jsSettings += formatEditorOptionJSBoolean("showPrintMargin", Settings.SHOW_PRINT_MARGIN);
        jsSettings += formatEditorOptionJS("printMarginColumn", Settings.PRINT_MARGIN_COLUMN);
        jsSettings += formatEditorOptionJSBoolean("showGutter", Settings.SHOW_GUTTER);
        jsSettings += formatEditorOptionJSBoolean("showInvisibles", Settings.SHOW_INVISIBLES);
        jsSettings += formatEditorOptionJSRaw("tabSize", Settings.TAB_SIZE);
        jsSettings += formatEditorOptionJSBoolean("useSoftTabs", Settings.USE_SOFT_TABS);
        jsSettings += formatEditorOptionJSBoolean("wrap", Settings.WRAP_TEXT);
        jsSettings += formatEditorOptionJSBoolean("showLineNumbers", Settings.SHOW_LINE_NUMBERS);

        if (jsSettings.endsWith(",\n")) {
            jsSettings = jsSettings.substring(0, jsSettings.length() - 2);
        }

        jsCode += jsSettings;
        jsCode += "\n});";

        jsCode += String.format("editor.session.setWrapLimit(%1$s);%n", settings.simple().get(Settings.SETTINGS_EDITOR, Settings.WRAP_LIMIT));
        jsCode += String.format("editor.setHighlightSelectedWord(%1$s);%n", settings.simple().getBoolean(Settings.SETTINGS_EDITOR, Settings.HIGHLIGHT_SELECTED_WORD));

        return jsCode;
    }

    /**
     * Display the release notes
     *
     * @throws IOException if error occurs when reading the changelog file
     */
    public void showReleaseNotes() throws IOException {
        String lastVersion = settings.simple().get(Settings.INFO, Settings.LAST_VERSION);

        if (lastVersion.compareTo(Loader.SHORT_VERSION) < 0) {

            String changeLogMD = FileUtils.readFileToString(new File("../CHANGELOG.md"));
            String changeLogHTML = new PegDownProcessor().markdownToHtml(changeLogMD);

            settings.simple().save(Settings.INFO, Settings.LAST_VERSION, Loader.SHORT_VERSION);

            ChangeLogDialog releaseNotes = new ChangeLogDialog("Change Log",
                    "Current version: " + Loader.SHORT_VERSION,
                    changeLogHTML);
            releaseNotes.initModality(Modality.APPLICATION_MODAL);
            releaseNotes.show();
        }
    }

    private void showMissingLicensePlugins() {
        List<PluginLoadFailure> missingLicensePlugins = Loader.getInitializer().getMissingLicensePlugins();
        if (!missingLicensePlugins.isEmpty()) {
            MissingLicensePluginsDialog missingLicensePluginsDialog = new MissingLicensePluginsDialog(missingLicensePlugins);
            missingLicensePluginsDialog.initModality(Modality.APPLICATION_MODAL);
            missingLicensePluginsDialog.show();
        }
    }

    @Override
    public void handle(final Event event) {

        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            KeyEvent keyEvent = (KeyEvent) event;

            Hotkeys hk = hotkeys.getHotkey(keyEvent);
            if (hk != null) {

                switch (hk) {
                    case CLOSE:
                        closeTab(tpnBots.getSelectionModel().getSelectedItem());
                        break;
                    case NEW:
                        buttonNewFile();
                        break;
                    case SAVE:
                        buttonSave();
                        break;
                    case SAVEAS:
                        buttonSaveAs();
                        break;
                    case SAVEALL:
                        buttonSaveAll();
                        break;
                    case OPEN:
                        buttonOpenFile();
                        break;
                    case CLEARCONSOLE:
                        tpnBots.getTabs().filtered(Tab::isSelected).forEach(tab -> {
                            ((RobotTab) tab).clearConsolePane();
                            keyEvent.consume();
                        });
                        break;
                    case RUN:
                        tpnBots.getTabs().filtered(Tab::isSelected).forEach(tab -> {
                            ((RobotTab) tab).getEditorPane().getControls().start();
                            keyEvent.consume();
                        });
                        break;
                    case STEPIN:
                        tpnBots.getTabs().filtered(Tab::isSelected).forEach(tab -> {
                            ((RobotTab) tab).getEditorPane().getControls().stepIn();
                            keyEvent.consume();
                        });
                        break;
                    case STEPOVER:
                        tpnBots.getTabs().filtered(Tab::isSelected).forEach(tab -> {
                            ((RobotTab) tab).getEditorPane().getControls().stepOver();
                            keyEvent.consume();
                        });
                        break;
                    case PAUSE:
                        tpnBots.getTabs().filtered(Tab::isSelected).forEach(tab -> {
                            ((RobotTab) tab).getEditorPane().getControls().pause(true);
                            keyEvent.consume();
                        });
                        break;
                    case STOP:
                        tpnBots.getTabs().filtered(Tab::isSelected).forEach(tab -> {
                            ((RobotTab) tab).getEditorPane().getControls().stop();
                            keyEvent.consume();
                        });
                        break;
                    case OPENSETTINGS:
                        buttonSettings();
                        break;
                    case ESCAPE:
                        // No editorpane has focus, so focus to a selected one
                        tpnBots.getTabs().filtered(Tab::isSelected).forEach(tab ->
                                ((FileTab) tab).getEditorPane().requestFocus());
                        break;
                    default:
                        if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
                            // Check if other key is an integer, if so open that tab
                            try {
                                int tab = Integer.parseInt(keyEvent.getText()) - 1;

                                if (tab < tpnBots.getTabs().size() && tab >= 0) {
                                    tpnBots.getSelectionModel().select(tab);
                                    ((FileTab) tpnBots.getTabs().get(tab)).requestFocus();
                                }
                            } catch (NumberFormatException e) {
                                // nevermind...
                            }
                        }
                }
            }
        }
    }

    /**
     * Close a tab
     *
     * @param tab RobotTab
     */
    public void closeTab(final Tab tab) {
        closeTab(tab, true, false);
    }

    /**
     * Close a tab
     *
     * @param tab The tab
     * @param removeTab True if tab will be removed
     * @param silent True if tab will be closed silently without any further dialogs
     */
    public void closeTab(final Tab tab, final boolean removeTab, final boolean silent) {
        // Stop if we don't have a selected tab
        if (tab == null) {
            return;
        }

        // Check for onClose handlers
        EventHandler<Event> handler = tab.getOnCloseRequest();
        Event closeEvent = new Event(silent ? RobotTab.SILENT_CLOSED_EVENT : Tab.CLOSED_EVENT);
        if (handler != null) {
            handler.handle(closeEvent);
        }

        // Remove the tab
        if (!closeEvent.isConsumed() && removeTab) {
            tpnBots.getTabs().remove(tab);
        }
    }

    /**
     * Close all tabs except one.
     *
     * @param tab The tab to keep open.
     */
    public void closeAllTabsExcept(final Tab tab) {
        List<FileTab> tabs = getTabs();
        tabs.stream().filter(t -> t != tab).forEach(this::closeTab);
    }

    /**
     * @return A list of active tabs
     */
    public List<FileTab> getTabs() {
        return tpnBots.getTabs().stream().map(tab -> (FileTab) tab).collect(Collectors.toList());
    }

    /**
     * Opens a tab if it can be found.
     *
     * @param tab a tab to open
     */
    public void showTab(final FileTab tab) {
        int index = tpnBots.getTabs().indexOf(tab);

        if (index >= 0) {
            tpnBots.getSelectionModel().clearAndSelect(index);
        }

        //a robot is opened so enable the save buttons
        disableSaveButtons(false);
    }

    /**
     * @return currently selected RobotTab
     */
    public Tab getSelectedTab() {
        return tpnBots.getSelectionModel().getSelectedItem();
    }

    /**
     * Finds the tab according to filePath (~RobotID.path)
     *
     * @param filePath filepath to robot (.xill) file
     * @return RobotTab if found, otherwise null
     */
    public FileTab findTab(final File filePath) {
        for (Tab tab : tpnBots.getTabs()) {
            if (((FileTab) tab).getDocument().equals(filePath)) {
                return (FileTab) tab;
            }
        }
        return null;
    }

    /**
     * @param cancelClose should be the closing of application interrupted?
     */
    public void setCancelClose(boolean cancelClose) {
        this.cancelClose = cancelClose;
    }

    /**
     * Disables the new file button
     *
     * @param disable boolean parameter to disable the new file button
     */
    public void disableNewFileButton(boolean disable) {
        btnNewFile.setDisable(disable);
    }

    /**
     * Disable the openFile button
     *
     * @param disable boolean parameter to disable the open file button
     */
    public void disableOpenFileButton(boolean disable) {
        btnOpenFile.setDisable(disable);
    }

    /**
     * Disable the save,save as and save all button
     *
     * @param disable boolean parameter to disable the save,save as and save all button
     */
    public void disableSaveButtons(boolean disable) {
        btnSaveAs.setDisable(disable);
        btnSaveAll.setDisable(disable);
        btnSave.setDisable(disable);
    }

    /**
     * Getter for the HelpPane
     *
     * @return the helppane
     */
    public HelpPane getHelpPane() {
        return helppane;
    }
}
