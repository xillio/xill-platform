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

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.elasticconsole.ESConsoleClient;
import nl.xillio.migrationtool.elasticconsole.RobotLogMessage;
import nl.xillio.migrationtool.gui.StatusBar.Status;
import nl.xillio.xill.util.HotkeysHandler;
import org.slf4j.Logger;

/**
 * The editor pane for robots. Contains most of the UI, apart from the left panel.
 */
public class RobotEditorPane extends EditorPane implements RobotTabComponent {
    private static final Logger LOGGER = Log.get();

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
    private CheckMenuItem cmiDebug;
    @FXML
    private CheckMenuItem cmiInfo;
    @FXML
    private CheckMenuItem cmiWarning;
    @FXML
    private CheckMenuItem cmiError;

    private RobotControls controls;

    /**
     * Default constructor. Just sets up the UI and the listener.
     */
    public RobotEditorPane() {
        super("/fxml/RobotEditorPane.fxml");
    }

    @Override
    public void initialize(final RobotTab tab) {
        super.initialize(tab);
        controls = new RobotControls(tab, btnRun, btnPause, btnStop, btnStepIn, btnStepOver, cmiError);
        ESConsoleClient.getLogEvent(tab.getProcessor().getRobotID()).addListener(this::onLogMessage);
    }

    private RobotTab robotTab() {
        return (RobotTab) tab;
    }

    private void onLogMessage(final RobotLogMessage message) {
        switch (message.getLevel()) {
            case "debug":
                if (cmiDebug.isSelected()) {
                    controls.pause(false);
                }
                break;
            case "info":
                if (cmiInfo.isSelected()) {
                    controls.pause(false);
                }
                break;
            case "warn":
                if (cmiWarning.isSelected()) {
                    controls.pause(false);
                }
                break;
            case "error":
                if (cmiError.isSelected()) {
                    controls.pause(false);
                }
                break;
            default:
                LOGGER.debug("Unimplemented loglevel: " + message.getLevel());
                break;
        }
    }

    @Override
    @FXML
    public void handle(final KeyEvent event) {
        super.handle(event);

        // Open help
        if (KeyCombination.valueOf(FXController.hotkeys.getShortcut(HotkeysHandler.Hotkeys.HELP)).match(event)) {
            // Get the plugin and construct at the caret.
            String[] values;
            try {
                values = getEditor().getPluginAndConstructAtCursor();
            } catch (ClassCastException e) {
                LOGGER.warn("Could not get plugin and construct at caret.", e);
                return;
            }

            // Open the appropriate help page.
            HelpPane helpPane = tab.getGlobalController().getHelpPane();
            if (values[0] == null) {
                helpPane.getHelpSearchBar().cleanup();
                helpPane.displayHome();
                helpPane.getHelpSearchBar().requestFocus();
            } else if (values[1] == null) {
                helpPane.display(values[0], "_index");
            } else {
                helpPane.display(values[0], values[1]);
            }

            event.consume();
        }
    }

    /**
     * Checks if the @newCode means that document is changed or not.
     * It compares the @newCode with the last saved editor's content.
     *
     * @param newCode the new code
     */
    @Override
    protected void updateDocumentState(final String newCode) {
        super.updateDocumentState(newCode);

        if (documentState.getValue() == DocumentState.CHANGED) {
            // Only trigger auto-save if we are ready to run
            Status currentStatus = robotTab().getStatusBar().statusProperty().get();

            if (currentStatus == null || currentStatus == Status.PAUSED || currentStatus == Status.READY || currentStatus == Status.STOPPED) {
                tab.resetAutoSave();
            }
        }
    }

    /**
     * @return the robot controls which allows to control the active robot
     */
    public RobotControls getControls() {
        return controls;
    }
}
