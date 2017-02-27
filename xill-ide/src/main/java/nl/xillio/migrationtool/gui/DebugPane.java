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

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import nl.xillio.xill.util.HotkeysHandler.Hotkeys;
import nl.xillio.xill.util.settings.Settings;
import nl.xillio.xill.util.settings.SettingsHandler;
import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * A collapsible debug pane. Contains the variable and the preview panes.
 */
public class DebugPane extends AnchorPane implements EventHandler<KeyEvent>, RobotTabComponent {
    private static final SettingsHandler settings = SettingsHandler.getSettingsHandler();
    private static final Logger LOGGER = Log.get();

    @FXML
    private VariablePane variablepane;
    @FXML
    private PreviewPane previewpane;
    @FXML
    private SplitPane spnBotRight;
    @FXML
    private InstructionStackPane instructionstackpane;

    /**
     * Default constructor.
     */
    public DebugPane() {
        super();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DebugPane.fxml"));
            loader.setClassLoader(getClass().getClassLoader());
            loader.setController(this);
            Node ui = loader.load();
            getChildren().add(ui);

        } catch (IOException e) {
            LOGGER.error("Error loading debug pane: " + e.getMessage(), e);
        }

        variablepane.setPreviewPane(previewpane);

        this.addEventHandler(KeyEvent.KEY_PRESSED, this);
    }

    @Override
    public void handle(final KeyEvent event) {
        if (KeyCombination.valueOf(FXController.hotkeys.getShortcut(Hotkeys.FIND)).match(event)) {
            previewpane.openSearch();
        }
    }

    @Override
    public void initialize(final RobotTab tab) {
        settings.simple().register(Settings.LAYOUT, Settings.PREVIEW_HEIGHT + getFullPath(tab), "0.6", "The height of the preview panel");
        // Load the divider position
        spnBotRight.setDividerPosition(0, Double.parseDouble(settings.simple().get(Settings.LAYOUT, Settings.PREVIEW_HEIGHT + getFullPath(tab))));
        spnBotRight.getDividers().get(0).positionProperty().addListener((observable, prevPos, newPos) -> settings.simple().save(Settings.LAYOUT, Settings.PREVIEW_HEIGHT + getFullPath(tab), Double.toString(newPos.doubleValue())));

        initializeChildren(tab);
    }

    public String getFullPath(RobotTab tab) {
        return tab.getProcessor().getRobotID().toString();
    }

    /**
     * Initialize graphical FX items that belongs to DebugPane (it's because of problem with Tab.getContent() on Linux, see CTC-713)
     *
     * @param tab currently active RobotTab
     */
    private void initializeChildren(final RobotTab tab) {
        instructionstackpane.initialize(tab);
        instructionstackpane.setDebugPane(this);
        variablepane.initialize(tab);
        variablepane.initialize(instructionstackpane);
        previewpane.initialize(tab);
    }

    public VariablePane getVariablepane() {
        return variablepane;
    }
}
