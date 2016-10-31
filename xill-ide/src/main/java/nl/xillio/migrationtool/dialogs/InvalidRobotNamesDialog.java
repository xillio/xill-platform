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
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

/**
 * A dialog to warn user about invalid robot names.
 */
public class InvalidRobotNamesDialog extends FXMLDialog {

    private boolean canceled = true;

    @FXML
    private Text txtTitle;
    @FXML
    private TextArea txtList;

    /**
     * Default constructor.
     *
     * @param list The text list of robots with invalid name.
     */
    public InvalidRobotNamesDialog(final String list) {
        super("/fxml/dialogs/InvalidRobotNames.fxml");
        setTitle("Upload to server");
        setResizable(false);
        txtList.setText(list);
    }

    /**
     * @return true if Cancel button or close sign has been pressed, otherwise Skip invalid has been pressed
     */
    public boolean isCanceled() {
        return canceled;
    }

    @FXML
    private void okBtnPressed(final ActionEvent event) {
        canceled = false;
        close();
    }
}
