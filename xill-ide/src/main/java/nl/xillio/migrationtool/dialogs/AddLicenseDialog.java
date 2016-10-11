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
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import nl.xillio.migrationtool.EulaUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AddLicenseDialog extends FXMLDialog {

    @FXML
    private CheckBox cbEulaAccepted;

    @FXML
    private Hyperlink hlEula;
    private boolean ok;

    public AddLicenseDialog() {
        super("/fxml/dialogs/AddLicense.fxml");
        setTitle("Accept End User License Agreement");
        hlEula.setText(EulaUtils.EULA_LOCATION);
    }

    @FXML
    private void cancelBtnPressed(final ActionEvent event) {
        close();
    }

    @FXML
    private void okayBtnPressed(final ActionEvent event) {
        if (cbEulaAccepted.isSelected()) {
            ok = true;
            close();
        } else {
            showError("You have to accept the End User License Agreement to run this software.", "EULA Not Accepted");
        }
    }

    @FXML
    private void hlEulaClicked(final ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI(EulaUtils.EULA_LOCATION));
        } catch (IOException | URISyntaxException e) {
            showError("Unfortunately your system is not able to open a link. Copy this link and paste it into a browser: " + EulaUtils.EULA_LOCATION, "Compatibility");
        }
    }

    private void showError(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setTitle(title);
        alert.initOwner(this.getScene().getWindow());
        alert.showAndWait();
    }

    public boolean getEulaChoice() {
        return ok && cbEulaAccepted.isSelected();
    }

}
