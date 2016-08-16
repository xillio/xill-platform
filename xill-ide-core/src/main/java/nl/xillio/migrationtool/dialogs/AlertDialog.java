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

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class AlertDialog extends Alert {

    private static final Logger LOGGER = Log.get();

    /**
     * Create a new alert dialog with the given alert type, title, header and content text, and a variable number of buttons.
     *
     * @param type    The alert type of the dialog.
     * @param title   The text to display in the title bar of the dialog.
     * @param header  The text to display in the header of the dialog.
     * @param content The text to display in the dialog content area.
     * @param buttons The buttons to add to the button-bar area of the dialog.
     */
    public AlertDialog(AlertType type, String title, String header, String content, ButtonType... buttons) {
        super(type, content, buttons);
        this.initStyle(StageStyle.UNIFIED);

        // Set the text.
        this.setTitle(title);
        this.setHeaderText(header);

        // Set the icon.
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        try (InputStream image = this.getClass().getResourceAsStream("/icon.png")) {
            if (image != null) {
                stage.getIcons().add(new Image(image));
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        // Hook each button into the enter pressed event.
        Arrays.stream(buttons).map(getDialogPane()::lookupButton).forEach(button -> button.addEventHandler(KeyEvent.KEY_PRESSED, this::enterPressed));
    }

    private void enterPressed(KeyEvent event) {
        // Check if enter was pressed and the target is a button.
        if (KeyCode.ENTER.equals(event.getCode()) && event.getTarget() instanceof Button) {
            ((Button) event.getTarget()).fire();
            event.consume();
        }
    }
}
