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

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import nl.xillio.xill.api.ProgressInfo;

import java.util.*;

public class RobotsProgressDialog extends FXMLDialog implements ChangeListener<Number> {

    /**
     * The class representing one row of the table
     */
    public class Row {
        private final StringProperty robotID;
        private final SimpleDoubleProperty progress;

        Row(String robotID, SimpleDoubleProperty progress) {
            this.robotID = new SimpleStringProperty(robotID);
            this.progress = progress;
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
    }

    @FXML
    private TableView<Row> tblRobotsProgress;
    @FXML
    private Button btnClose;

    private ObservableList<Row> table = FXCollections.observableArrayList();
    private Timer refreshTimer = new Timer();
    private boolean progressChanged = false;
    private boolean progressRemove = false;

    public RobotsProgressDialog(final ObservableList<Tab> tabs) {
        super("/fxml/dialogs/RobotsProgress.fxml");
        setTitle("List of robots progress");

        // Iterate all currently open robot tabs and add to list those having progress bar active
        tabs.filtered(t -> t instanceof RobotTab).forEach(t -> {
            RobotTab tab = (RobotTab)t;
            ProgressInfo progressInfo = tab.getStatusBar().getProgressInfo();

            // Check if progress is used and if so then add it to the table
            if (!(progressInfo == null || progressInfo.getProgress() < 0)) {
                table.add(new Row(tab.getName(), tab.getStatusBar().progressProperty()));
                tab.getStatusBar().progressProperty().addListener(this);
            }
        });

        // Create Robot ID column
        TableColumn<Row, String> colRobotID = new TableColumn<>("Robot ID");
        colRobotID.setCellValueFactory(new PropertyValueFactory<Row, String>("robotID"));
        colRobotID.setMinWidth(200);
        colRobotID.setPrefWidth(250);
        colRobotID.setResizable(true);

        // Create Progress column
        TableColumn<Row, Double> colProgress = new TableColumn<>("Progress");
        colProgress.setCellValueFactory(new PropertyValueFactory<Row, Double>("progress"));
        colProgress.setCellFactory(ProgressBarTableCell.<Row> forTableColumn());
        colProgress.setMinWidth(200);
        colProgress.setPrefWidth(250);
        colProgress.setResizable(true);

        // Add columns and items
        tblRobotsProgress.getColumns().addAll(colRobotID, colProgress);
        tblRobotsProgress.setItems(table);

        // Define refresh task
        TimerTask refreshTask = new TimerTask() {
            @Override
            public void run() {
                if (progressChanged) { // If some progress bar value has been changed, do the table refresh
                    if (progressRemove) { // If some progress bar has been hidden, remove corresponding row from table
                        final LinkedList<Row> removeList = new LinkedList<>();
                        table.forEach(row -> {
                            if (row.getProgress() < 0) {
                                removeList.add(row);
                            }
                        });
                        table.removeAll(removeList);
                        progressRemove = false;
                    }

                    tblRobotsProgress.refresh();
                    progressChanged = false;
                }
            }
        };
        refreshTimer.scheduleAtFixedRate(refreshTask, 0, 1000); // 1 second repeat refresh
    }

    @FXML
    private void closeBtnPressed(final ActionEvent event) {
        close();
    }

    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        progressChanged = true;
        if (newValue.doubleValue() < 0) {
            progressRemove = true;
        }
    }
}