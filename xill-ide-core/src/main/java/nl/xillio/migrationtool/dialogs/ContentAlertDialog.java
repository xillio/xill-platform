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
package nl.xillio.migrationtool.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * A custom dialog which contains a gridPane to add custom made elements to.
 *
 * @author ThomTrignol
 */
public class ContentAlertDialog extends AlertDialog {

    @FXML
    private VBox vBox;

    private GridPane gridPane;

    /**
     * Create a new alert dialog with the given alert type, title, header and content text, and a variable number of buttons.
     *
     * @param type     The alert type of the dialog.
     * @param title    The text to display in the title bar of the dialog.
     * @param header   The text to display in the header of the dialog.
     * @param content  The text to display in the dialog content area.
     * @param gridPane The gridPane used as content holder.
     * @param buttons  The buttons to add to the button-bar area of the dialog.
     */
    public ContentAlertDialog(Alert.AlertType type, String title, String header, String content, GridPane gridPane, ButtonType... buttons) {
        super("/fxml/dialogs/ContentAlertDialog.fxml", type, title, header, content, buttons);

        vBox.getChildren().add(gridPane);
    }

    /**
     * @return The GridPane provided in the constructor.
     */
    public GridPane getGridPane() {
        return gridPane;
    }
}
