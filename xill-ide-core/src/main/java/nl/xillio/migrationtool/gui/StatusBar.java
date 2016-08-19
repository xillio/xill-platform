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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import nl.xillio.events.Event;
import nl.xillio.xill.api.Debugger;
import me.biesaart.utils.Log;
import nl.xillio.xill.api.ProgressInfo;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * A status bar, that can be used to represent the progress of the current running robot.
 */
public class StatusBar extends AnchorPane {

    // Used enum for statuses instead of the strings previously implemented
    public enum Status {
        RUNNING("Running"),
        STOPPING("Stopping..."),
        STOPPED("Stopped"),
        PAUSING("Pausing..."),
        PAUSED("Paused"),
        COMPILING("Compiling"),
        READY("Ready");


        private String representation;

        Status(String representation) {
            this.representation = representation;
        }

        @Override
        public String toString() {
            return representation;
        }
    }

    private static final Logger LOGGER = Log.get();

    @FXML
    private ProgressBar barRobotProgress;
    private final SimpleDoubleProperty progress = new SimpleDoubleProperty();
    private ProgressInfo progressInfo;
    @FXML
    private Labeled lblTimeRemaining;
    @FXML
    private Labeled lblStatusVal;
    private final ObjectProperty<Status> status = new SimpleObjectProperty<>(Status.READY);

    /**
     * Default constructor. A robot has to be attached to the component using setProcessor.
     */
    public StatusBar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StatusBar.fxml"));
            loader.setClassLoader(getClass().getClassLoader());
            loader.setController(this);
            Node ui = loader.load();
            getChildren().add(ui);
            lblStatusVal.textProperty().bind(status.asString());
            barRobotProgress.progressProperty().bind(progress);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Adds event listeners used to display status to the debugger
     *
     * @param debugger The debugger to get the {@link Event Events} from
     */
    public void registerDebugger(Debugger debugger) {
        debugger.getOnRobotStart().addListener(e -> setStatus(Status.RUNNING));
        debugger.getOnRobotStop().addListener(e -> {
            setStatus(Status.STOPPED);
            // Set progress bar according to its settings
            if (progressInfo != null) {
                switch (progressInfo.getOnStopBehavior()) {
                    case ZERO: // Set to zero
                        progress.set(0);
                        break;
                    case HIDE: // Hide progress bar
                        barRobotProgress.setVisible(false);
                        progressInfo.setProgress(-1);
                        progress.set(-1);
                        break;
                    default: // Otherwise do nothing
                }
            }
        });
        debugger.getOnRobotPause().addListener(e -> setStatus(Status.PAUSED));
        debugger.getOnRobotContinue().addListener(e -> setStatus(Status.RUNNING));
        debugger.getOnSetProgressInfo().addListener(e -> setProgress(e));
    }

    /**
     * Set the status of the status bar based on the robot action
     *
     * @param status The actual status to be displayed on the status bar
     */
    public void setStatus(Status status) {
        Platform.runLater(() -> this.status.set(status));
    }

    public ObjectProperty<Status> statusProperty() {
        return status;
    }

    /**
     * This method is called when event is invoked in XillDebugger.setProgressInfo()
     *
     * @param progressInfo the ProgressInfo object instantiated in XillDebugger
     */
    private void setProgress(ProgressInfo progressInfo) {
        this.progressInfo = progressInfo;
        double newProgress = progressInfo.getProgress();
        barRobotProgress.setVisible(newProgress >= 0);
        progress.set(newProgress);
    }

    /**
     * Getter for progress value property
     *
     * @return the progress value property
     */
    public SimpleDoubleProperty progressProperty() {
        return progress;
    }

    /**
     * Getter for ProgressInfo object
     *
     * @return the ProgressInfo object
     */
    public ProgressInfo getProgressInfo() {
        return progressInfo;
    }
}
