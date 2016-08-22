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

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import nl.xillio.migrationtool.gui.RobotTab;
import nl.xillio.migrationtool.gui.StatusBar;
import nl.xillio.xill.api.ProgressInfo;

import java.util.function.Consumer;

public class RobotsProgressDialog extends FXMLDialog {

    /**
     * The class representing one row of the table
     */
    public class Row {
        private final StringProperty robotID;
        private final SimpleDoubleProperty progress;
        private final StatusBar statusBar;
        private final Consumer<StatusBar> removalListener;

        Row(String robotID, StatusBar statusBar, Consumer<StatusBar> removalListener) {
            this.robotID = new SimpleStringProperty(robotID);
            this.progress = new SimpleDoubleProperty();
            this.progress.bind(statusBar.progressProperty());
            this.statusBar = statusBar;
            this.removalListener = removalListener;
        }

        public void setProgress(double progress) {
            this.progress.set(progress);
        }

        public String getRobotID() {
            return robotID.get();
        }

        public double getProgress() {
            return progress.get();
        }

        public SimpleDoubleProperty progressProperty() {
            return progress;
        }

        public StatusBar getStatusBar() {
            return statusBar;
        }

        public Consumer<StatusBar> getRemovalListener() {
            return removalListener;
        }
    }

    @FXML
    private TableView<Row> tblRobotsProgress;
    @FXML
    private Button btnClose;

    private ObservableList<Row> table = FXCollections.observableArrayList();

    public RobotsProgressDialog(final ObservableList<Tab> tabs) {
        super("/fxml/dialogs/RobotsProgress.fxml");
        setTitle("List of robots progress");

        // Iterate all currently open robot tabs and add to list those having progress bar active
        tabs.filtered(t -> t instanceof RobotTab).forEach(t -> {
            RobotTab tab = (RobotTab)t;
            ProgressInfo progressInfo = tab.getStatusBar().getProgressInfo();

            // Check if progress is used and if so then add it to the table
            if (!(progressInfo == null || progressInfo.getProgress() < 0)) {

                // Define listener for progress bar removal event
                Consumer<StatusBar> removalListener = statusBar -> {
                    Platform.runLater(() -> {
                        Row row = table.stream().filter(p -> p.getStatusBar() == tab.getStatusBar()).findFirst().orElse(null);
                        if (row != null) {
                            row.progressProperty().unbind();
                            row.getStatusBar().getOnProgressRemove().removeListener(row.getRemovalListener());
                            table.remove(row);
                        }
                    });
                };

                // Add new progress bar item to table ans set listener
                table.add(new Row(tab.getName(), tab.getStatusBar(), removalListener));
                tab.getStatusBar().getOnProgressRemove().addListener(removalListener);
            }
        });

        // Create Robot ID column
        TableColumn<Row, String> colRobotID = new TableColumn<>("Robot ID");
        colRobotID.setCellValueFactory(new PropertyValueFactory<>("robotID"));
        colRobotID.setMinWidth(200);
        colRobotID.setPrefWidth(250);
        colRobotID.setResizable(true);

        // Create Progress column
        TableColumn<Row, Double> colProgress = new TableColumn<>("Progress");
        colProgress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        colProgress.setCellFactory(ProgressBarTableCell.forTableColumn());
        colProgress.setMinWidth(200);
        colProgress.setPrefWidth(250);
        colProgress.setResizable(true);

        // Add columns and items
        tblRobotsProgress.getColumns().addAll(colRobotID, colProgress);
        tblRobotsProgress.setItems(table);

        // Define action when dialog is about to close
        setOnCloseRequest(e -> {
           table.forEach(row -> row.getStatusBar().getOnProgressRemove().removeListener(row.getRemovalListener()));
        });
    }

    @FXML
    private void closeBtnPressed(final ActionEvent event) {
        close();
    }
}