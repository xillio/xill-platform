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

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;
import me.biesaart.utils.Log;
import nl.xillio.events.Event;
import nl.xillio.migrationtool.Loader;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.services.ProgressTracker;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.UUID;

/**
 * A status bar, that can be used to represent the progress of the current running robot.
 */
public class StatusBar extends AnchorPane {

    private ProgressTracker progressTracker;
    private Timeline progressTimeline;
    private boolean progressBarVisible = false;
    private UUID robotCSID;
    private String robotName;

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
    private final SimpleDoubleProperty progressProperty = new SimpleDoubleProperty();

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
            barRobotProgress.progressProperty().bind(progressProperty);
            progressProperty.set(-1);

            Platform.runLater(() -> {
                progressTimeline = new Timeline(new KeyFrame(Duration.millis(1000), a -> updateProgress()));
                progressTimeline.setCycleCount(Animation.INDEFINITE);
            });
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        progressTracker = Loader.getXill().getProgressTracker();
    }

    /**
     * Adds event listeners used to display status to the debugger
     *
     * @param debugger The debugger to get the {@link Event Events} from
     */
    public void registerDebugger(Debugger debugger) {
        debugger.getOnRobotStart().addListener(e -> {
            robotCSID = e.getCompilerSerialID();
            progressTimeline.play();
            setStatus(Status.RUNNING);
        });
        debugger.getOnRobotStop().addListener(e -> {
            setStatus(Status.STOPPED);
            progressTimeline.stop();
            // Set progress bar according to its settings
            ProgressTracker.OnStopBehavior onStopBehavior = progressTracker.getOnStopBehavior(robotCSID);
            if (onStopBehavior != null) {
                switch (onStopBehavior) {
                    case ZERO: // Set to zero
                        progressProperty().set(0);
                        break;
                    case HIDE: // Hide progress bar
                        barRobotProgress.setVisible(false);
                        progressBarVisible = false;
                        progressProperty().set(-1);
                        FXController.ON_PROGRESS_REMOVE.invoke(this);
                        break;
                    default: // Otherwise do nothing
                }
            }
            progressTracker.remove(robotCSID);
            robotCSID = null;
        });
        debugger.getOnRobotPause().addListener(e -> setStatus(Status.PAUSED));
        debugger.getOnRobotContinue().addListener(e -> setStatus(Status.RUNNING));
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
     * Getter for progress value property
     *
     * @return the progress value property
     */
    public SimpleDoubleProperty progressProperty() {
        return progressProperty;
    }

    private void updateProgress() {
        if (robotCSID == null) {
            return;
        }

        double progress = progressTracker.getProgress(robotCSID);
        if (progress >= 0) {
            // Set new progress
            if (!progressBarVisible) {
                barRobotProgress.setVisible(true);
                progressBarVisible = true;
                FXController.ON_PROGRESS_ADD.invoke(this);
            }
            progressProperty.set(progress);
        } else {
            // Hide progress bar
            if (progressBarVisible) {
                barRobotProgress.setVisible(false);
                progressBarVisible = false;
                FXController.ON_PROGRESS_REMOVE.invoke(this);
            }
            progressProperty.set(0);
        }
    }

    /**
     * Gets a robot name.
     *
     * @return robot name
     */
    public String getRobotName() {
        return robotName;
    }

    /**
     * Sets a robot name.
     *
     * @param robotName The robot name
     */
    public void setRobotName(final String robotName) {
        this.robotName = robotName;
    }
}
