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
import javafx.stage.Modality;
import nl.xillio.migrationtool.gui.FXController;
import nl.xillio.migrationtool.gui.RobotTab;
import nl.xillio.migrationtool.gui.StatusBar;

import java.util.function.Consumer;

public class RobotsProgressDialog extends FXMLDialog {

    /**
     * The class representing one row of the table
     */
    public class Row {
        private final StringProperty robotID;
        private final SimpleDoubleProperty progress;
        private final SimpleStringProperty remainingTime;
        private final StatusBar statusBar;

        Row(String robotID, StatusBar statusBar) {
            this.robotID = new SimpleStringProperty(robotID);
            this.progress = new SimpleDoubleProperty();
            this.progress.bind(statusBar.progressProperty());
            this.remainingTime = new SimpleStringProperty();
            this.remainingTime.bind(statusBar.remainingTimeProperty());
            this.statusBar = statusBar;
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

        public SimpleStringProperty remainingTimeProperty() {
            return remainingTime;
        }

        public StatusBar getStatusBar() {
            return statusBar;
        }
    }

    @FXML
    private TableView<Row> tblRobotsProgress;
    @FXML
    private Button btnClose;

    private final ObservableList<Row> table = FXCollections.observableArrayList();
    private final Consumer<StatusBar> addListener;
    private final Consumer<StatusBar> removalListener;

    public RobotsProgressDialog(final ObservableList<Tab> tabs) {
        super("/fxml/dialogs/RobotsProgress.fxml");
        setTitle("List of robots progress");
        this.initModality(Modality.NONE);

        addListener = this::addProgressBar;

        // Define listener for progress bar removal event
        removalListener = statusBar ->
                Platform.runLater(() -> {
                    Row row = table.stream().filter(p -> p.getStatusBar() == statusBar).findFirst().orElse(null);
                    if (row != null) {
                        row.progressProperty().unbind();
                        row.remainingTimeProperty().unbind();
                        table.remove(row);
                    }
                });

        // Iterate all currently open robot tabs and add to list those having progress bar active
        tabs.filtered(t -> t instanceof RobotTab).forEach(t -> {
            RobotTab tab = (RobotTab) t;
            // Check if progress is used and if so then add it to the table
            if (tab.getStatusBar().progressProperty().get() >= 0) {
                addProgressBar(tab.getStatusBar());
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

        // Create Remaining time column
        TableColumn<Row, Double> colRemainingTime = new TableColumn<>("Remaining time");
        colRemainingTime.setCellValueFactory(new PropertyValueFactory<>("remainingTime"));
        colRemainingTime.setMinWidth(100);
        colRemainingTime.setPrefWidth(150);
        colRemainingTime.setResizable(true);

        // Add columns and items
        tblRobotsProgress.getColumns().addAll(colRobotID, colProgress, colRemainingTime);
        tblRobotsProgress.setItems(table);

        // Define action when dialog is about to close
        setOnCloseRequest(e -> {
            // Remove listeners
            FXController.ON_PROGRESS_ADD.getEvent().removeListener(addListener);
            FXController.ON_PROGRESS_REMOVE.getEvent().removeListener(removalListener);
        });

        // Add listeners
        FXController.ON_PROGRESS_ADD.getEvent().addListener(addListener);
        FXController.ON_PROGRESS_REMOVE.getEvent().addListener(removalListener);
    }

    private void addProgressBar(final StatusBar bar) {
        // Check if given progress bar already exist
        if (table.stream().filter(p -> p.getStatusBar() == bar).findFirst().orElse(null) != null) {
            return;
        }

        // Add new progress bar item to table ans set listener
        table.add(new Row(bar.getRobotName(), bar));
    }

    @FXML
    private void closeBtnPressed(final ActionEvent event) {
        close();
    }
}