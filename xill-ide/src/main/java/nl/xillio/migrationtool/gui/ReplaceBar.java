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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import me.biesaart.utils.Log;
import nl.xillio.xill.api.preview.Replaceable;
import nl.xillio.xill.api.preview.Searchable;
import org.slf4j.Logger;

import java.io.IOException;

public class ReplaceBar extends SearchBar {

    private static final Logger LOGGER = Log.get();

    @FXML
    private TextField tfEditorReplaceString;

    public ReplaceBar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReplaceBar.fxml"));
            loader.setClassLoader(getClass().getClassLoader());
            loader.setController(this);
            Node ui = loader.load();
            ((VBox) getChildren().get(0)).getChildren().add(ui);
        } catch (IOException e) {
            LOGGER.error("Error loading replace bar: " + e.getMessage(), e);
        }

        // Add event handlers to the text fields.
        tfEditorSearchQuery.addEventHandler(KeyEvent.KEY_PRESSED, this);
        tfEditorReplaceString.addEventHandler(KeyEvent.KEY_PRESSED, this);
    }

    @Override
    public void setSearchable(final Searchable searchable) {
        if (searchable instanceof Replaceable) {
            super.setSearchable(searchable);
        }
    }

    public Replaceable getReplaceable() {
        return (Replaceable) super.getSearchable();
    }

    public String getReplacement() {
        return tfEditorReplaceString.getText();
    }

    @FXML
    private void onReplace(final ActionEvent actionEvent) {
        getReplaceable().replaceOne(currentOccurrence, getReplacement());
    }

    @FXML
    private void onReplaceAll(final ActionEvent actionEvent) {
        getReplaceable().replaceAll(getReplacement());
    }

    @Override
    public void handle(final KeyEvent keyEvent) {
        if (isChildFocused() && !keyEvent.isConsumed()) {
            switch (keyEvent.getCode()) {
                case TAB:
                    // Let tab switch focus between search and replace textfield.
                    toggleTextfieldFocus();
                    keyEvent.consume();
                    break;
                case ENTER:
                    if (tfEditorReplaceString.isFocused()) {
                        onReplace(null);
                    }
                    break;
            }
        }

        super.handle(keyEvent);
    }

    private void toggleTextfieldFocus() {
        if (this.tfEditorSearchQuery.isFocused()) {
            tfEditorReplaceString.requestFocus();
        } else {
            this.tfEditorSearchQuery.requestFocus();
        }
    }
}
