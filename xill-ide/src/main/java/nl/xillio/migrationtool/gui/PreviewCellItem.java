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
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Pair;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.NotImplementedException;

/**
 * Provides textual representation of variable expressions.
 *
 * @author Folkert van Verseveld
 */
public class PreviewCellItem extends TreeTableCell<Pair<String, MetaExpression>, MetaExpression> {

    public PreviewCellItem() {

        setOnMouseClicked(new DoubleClickEventHandler(this));
        createText();
        // Update width
        Platform.runLater(() -> setWidth(getWidth() + 0.1));

    }

    @Override
    protected void updateItem(MetaExpression item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null) {
            setText(null);
        } else {
            setText(ObservableVariable.expressionToString(item));
        }
    }

    /**
     * Create the contents of this cell as {@link Text}
     */
    public void createText() {
        Text text = new Text();
        text.wrappingWidthProperty().bind(this.widthProperty());
        text.textProperty().bind(this.textProperty());

        setGraphic(text);
    }

    /**
     * Create the contents of this cell as {@link TextArea}, useful for selecting text partially
     */
    public void createTextArea() {
        if (this.getItem().getType() == ExpressionDataType.ATOMIC) {
            TextArea textArea = new TextArea();
            textArea.setWrapText(true);
            textArea.textProperty().bind(this.textProperty());

            // Keep the same height as before
            textArea.setPrefHeight(getHeight());

            textArea.prefWidthProperty().bind(this.widthProperty());


            // Just show text when focus is lost
            textArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    createText();
                }
            });

            setGraphic(textArea);

            // Request focus to prevent TextArea from staying visible
            textArea.requestFocus();
        }
    }

    private class DoubleClickEventHandler implements EventHandler<MouseEvent> {
        private final PreviewCellItem tableCell;

        public DoubleClickEventHandler(final PreviewCellItem tableCell) {
            this.tableCell = tableCell;
        }

        @Override
        public void handle(MouseEvent event) {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && tableCell.getItem() != null) {
                tableCell.createTextArea();
            }
        }
    }
}