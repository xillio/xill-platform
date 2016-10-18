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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.util.Optional;

public class AlertDialog extends FXMLDialog {

    private static final Logger LOGGER = Log.get();

    @FXML
    private Text header;

    @FXML
    private Text content;

    @FXML
    private Text seperation;

    @FXML
    private Button yesBtn;

    @FXML
    private Button noBtn;

    @FXML
    private Button applyBtn;

    @FXML
    private Button okBtn;

    @FXML
    private Button cancelBtn;

    private ButtonType pressedButtonType = null;

    /**
     * Create a new alert dialog with the given alert type, title, header and content text, and a variable number of buttons.
     *
     * @param type    The alert type of the dialog.
     * @param title   The text to display in the title bar of the dialog.
     * @param header  The text to display in the header of the dialog.
     * @param content The text to display in the dialog content area.
     * @param buttons The buttons to add to the button-bar area of the dialog.
     */
    public AlertDialog(Alert.AlertType type, String title, String header, String content, ButtonType... buttons) {
        super("/fxml/dialogs/AlertDialog.fxml");

        init(type, title, header, content, buttons);
    }

    /**
     * Used to initialize children.
     *
     * @param url Used to provice FXMLDialog with a URL.
     */
    protected AlertDialog(String url, Alert.AlertType type, String title, String header, String content, ButtonType... buttons) {
        super(url);

        init(type, title, header, content, buttons);
    }

    private void init(Alert.AlertType type, String title, String header, String content, ButtonType... buttons) {
        disableAllButtons();
        if(!enableOverrideButtons(buttons))
        {
            enableDefaultButtons(type);
        }

        this.setResizable(false);
        this.setTitle(title);
        this.header.setText(header);
        this.content.setText(content);

        if(!header.isEmpty() && !content.isEmpty())
        {
            seperation.setText("\n\n");
        }
    }

    private void enableDefaultButtons(Alert.AlertType type) {
        switch (type)
        {
            case NONE:
                okBtn.setVisible(true);
                break;
            case INFORMATION:
                okBtn.setVisible(true);
                break;
            case WARNING:
                okBtn.setVisible(true);
                break;
            case CONFIRMATION:
                okBtn.setVisible(true);
                cancelBtn.setVisible(true);
                break;
            case ERROR:
                okBtn.setVisible(true);
                break;
        }
    }

    /**
     * @return The type of the used button, if present.
     */
    public Optional<ButtonType> getResult() {
        return Optional.ofNullable(pressedButtonType);
    }

    private void disableAllButtons() {
        yesBtn.setVisible(false);
        noBtn.setVisible(false);
        applyBtn.setVisible(false);
        okBtn.setVisible(false);
        cancelBtn.setVisible(false);
    }

    private boolean enableOverrideButtons(ButtonType... buttons) {
        boolean buttonOverride = false;

        for (ButtonType button : buttons)
        {
            switch (button.getButtonData())
            {
                case YES:
                    buttonOverride = true;
                    yesBtn.setVisible(true);
                    break;
                case NO:
                    buttonOverride = true;
                    noBtn.setVisible(true);
                    break;
                case OK_DONE:
                    buttonOverride = true;
                    okBtn.setVisible(true);
                    break;
                case APPLY:
                    buttonOverride = true;
                    applyBtn.setVisible(true);
                    break;
                case CANCEL_CLOSE:
                    buttonOverride = true;
                    cancelBtn.setVisible(true);
                    break;
            }
        }

        return buttonOverride;
    }

    @FXML
    private void cancelBtnPressed(final ActionEvent event) {
        pressedButtonType = ButtonType.CANCEL;
        close();
    }

    @FXML
    private void okayBtnPressed(final ActionEvent event) {
        pressedButtonType = ButtonType.OK;
        close();
    }

    @FXML
    private void yesBtnPressed(final ActionEvent event) {
        pressedButtonType = ButtonType.YES;
        close();
    }

    @FXML
    private void noBtnPressed(final ActionEvent event) {
        pressedButtonType = ButtonType.NO;
        close();
    }

    @FXML
    private void applyBtnPressed(final ActionEvent event) {
        pressedButtonType = ButtonType.APPLY;
        close();
    }
}
