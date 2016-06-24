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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.TabPane;
import javafx.scene.web.WebView;

/**
 * Listener that can be attached to a {@link Node#focusedProperty()} that will return focus to the previous focus owner.
 * <p>
 * Note: This listener does not return focus to buttons
 *
 * @author Geert Konijnendijk, Sander Visser
 *         <p>
 */
public class ReturnFocusListener implements ChangeListener<Boolean> {
    // The previous owner of the focus
    private Node previousFocusOwner;

    public ReturnFocusListener(Scene scene) {
        // Keep track of the previous focus owner
        scene.focusOwnerProperty().addListener((observable, oldValue, newValue) -> {
            // Do not return focus to buttons
            previousFocusOwner = oldValue instanceof ButtonBase ? previousFocusOwner : oldValue;

            //set the focus on a new editor when changing robot.
            if (oldValue instanceof WebView && newValue instanceof TabPane) {
                ((TabPane) newValue).getTabs().forEach(tab -> {
                    if (tab.isSelected()) {
                        ((RobotTab) tab).requestFocus();
                    }
                });
            }
        });
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        //if focused return focus.
        if (newValue) {
            Platform.runLater(() -> {
                if (previousFocusOwner != null) {
                    previousFocusOwner.requestFocus();
                }
            });
        }
    }
}