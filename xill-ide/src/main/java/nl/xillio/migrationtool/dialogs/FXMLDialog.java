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

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.biesaart.utils.Log;
import nl.xillio.contenttools.Application;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A general dialog.
 */
public class FXMLDialog extends Stage {

    private static final Logger LOGGER = Log.get();

    /**
     * Default constructor.
     *
     * @param url the path to the fxml resource to load
     */
    public FXMLDialog(final String url) {
        loadFXML(getClass().getResource(url));
        initModality(Modality.APPLICATION_MODAL);
        Scene scene = Application.getPrimaryStage().getScene();
        initOwner(scene == null ? null : scene.getWindow());

        try (InputStream image = this.getClass().getResourceAsStream("/icon.png")) {
            if (image != null) {
                this.getIcons().add(new Image(image));
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        // Hook into the enter pressed event.
        this.getScene().addEventHandler(KeyEvent.KEY_PRESSED, this::enterPressed);
    }

    private void loadFXML(final URL resource) {
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setClassLoader(getClass().getClassLoader());
        loader.setController(this);

        try {
            setScene(new Scene(loader.load()));
        } catch (IOException e) {
            LOGGER.error("Failed to load FXML.", e);
        }
    }

    private void enterPressed(KeyEvent event) {
        // Check if enter was pressed and the target is a button.
        if (KeyCode.ENTER.equals(event.getCode()) && event.getTarget() instanceof Button) {
            ((Button) event.getTarget()).fire();
            event.consume();
        }
    }
}
