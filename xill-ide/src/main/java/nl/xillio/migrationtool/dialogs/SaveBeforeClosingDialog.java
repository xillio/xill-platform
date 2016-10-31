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
import javafx.event.Event;
import javafx.fxml.FXML;
import nl.xillio.migrationtool.gui.FileTab;

/**
 * A dialog to ask for saving upon tab closing.
 */
public class SaveBeforeClosingDialog extends FXMLDialog {
    private final FileTab tab;
    private final Event closeEvent;
    private boolean cancelPressed = false;

    /**
     * Default constructor.
     *
     * @param tab        the tab to close
     * @param closeEvent the original event
     */
    public SaveBeforeClosingDialog(final FileTab tab, final Event closeEvent) {
        super("/fxml/dialogs/SaveBeforeClosing.fxml");
        this.tab = tab;
        this.closeEvent = closeEvent;
        setTitle("Save changes to " + tab.getText() + "?");
    }

    @FXML
    private void yesBtnPressed(final ActionEvent event) {
        // Try to save, don't close the tab if it failed
        if (!tab.save()) {
            closeEvent.consume();
        }
        close();
    }

    @FXML
    private void noBtnPressed(final ActionEvent event) {
        close();
    }

    @FXML
    private void cancelBtnPressed(final ActionEvent event) {
        closeEvent.consume();
        cancelPressed = true;
        close();
    }

    /**
     * @return if the Cancel button has been pressed
     */
    public boolean isCancelPressed() {
        return cancelPressed;
    }
}
