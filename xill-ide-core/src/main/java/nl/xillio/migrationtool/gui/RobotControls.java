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
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.BreakpointPool;
import nl.xillio.xill.api.Breakpoint;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.LogUtil;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.errors.ErrorHandlingPolicy;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.errors.XillParsingException;
import nl.xillio.xill.util.HotkeysHandler.Hotkeys;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * This class handles the activating and deactivating of robot control buttons
 * like play and pause according to the current state of the robot.
 */
public class RobotControls implements EventHandler<KeyEvent>, ErrorHandlingPolicy {

    private static final Logger LOGGER = Log.get();

    private final Button start;
    private final Button stop;
    private final Button pause;
    private final Button stepin;
    private final Button stepover;
    private final RobotTab tab;
    private boolean running = false;
    private final CheckMenuItem shouldStopOnError;

    /**
     * Create a new {@link RobotControls}
     *
     * @param tab      the regarding robot tab
     * @param start    the fxml start button
     * @param pause    the fxml pause button
     * @param stop     the fxml stop button
     * @param stepin   the fxml stepin button
     * @param stepover the fxml stepover button
     * @param cmiError the item regarding if log-level error is checked or not
     */
    public RobotControls(final RobotTab tab, final Button start, final Button pause, final Button stop,
                         final Button stepin, final Button stepover, final CheckMenuItem cmiError) {
        this.tab = tab;
        this.start = start;
        this.stop = stop;
        this.pause = pause;
        this.stepin = stepin;
        this.stepover = stepover;
        shouldStopOnError = cmiError;

        start.setDisable(false);
        stop.setDisable(true);
        pause.setDisable(true);
        stepin.setDisable(true);
        stepover.setDisable(true);

        tab.getEditorPane().addEventHandler(KeyEvent.KEY_PRESSED, this);

        // Connect to buttons
        start.setOnAction(e -> start());
        stop.setOnAction(e -> stop());
        pause.setOnAction(e -> pause(true));
        stepin.setOnAction(e -> stepIn());
        stepover.setOnAction(e -> stepOver());

        // Connect to debugger
        getDebugger().getOnRobotContinue().addListener(e -> onContinue());
        getDebugger().getOnRobotStart().addListener(e -> onStart());
        getDebugger().getOnRobotStop().addListener(e -> onStop());
        getDebugger().getOnRobotPause().addListener(w -> onPause());

        getDebugger().setErrorHandler(this);
    }

    /**
     * Checks if the robot is running or not.
     *
     * @return Whether the robot is running
     */
    public boolean robotRunning() {
        return running;
    }

    /**
     * Start/Resume the robot
     */
    @SuppressWarnings("squid:S1166") // XillParsingException is handled correctly here
    public void start() {
        if (running) {
            continu();
            return;
        }

        try {
            applyBreakpoints();
            tab.runRobot();
        } catch (XillParsingException e) {
            getDebugger().stop();
            highlight(e.getRobot(), e.getLine(), "error");
        }

    }

    private void onStart() {
        resetAll();

        start.setDisable(true);
        stop.setDisable(false);
        pause.setDisable(false);
        running = true;
    }

    private void continu() {
        applyBreakpoints();
        getDebugger().resume();
    }

    private void onContinue() {
        resetAll();

        start.setDisable(true);
        stop.setDisable(false);
        pause.setDisable(false);
    }

    /**
     * Pause the robot
     *
     * @param userAction whether the pause action is triggered by the user or not
     */
    public void pause(boolean userAction) {
        disableAll(StatusBar.Status.PAUSING);
        getDebugger().pause(userAction);
    }

    private void onPause() {
        resetAll();

        stop.setDisable(false);
        stepin.setDisable(false);
        stepover.setDisable(false);
    }

    /**
     * Stop the robot
     */
    public void stop() {
        disableAll(StatusBar.Status.STOPPING);
        getDebugger().stop();
    }

    void onStop() {
        resetAll();
        running = false;

        Platform.runLater(tab::resetCode);
    }

    public void stepIn() {
        getDebugger().stepIn();
    }

    public void stepOver() {
        getDebugger().stepOver();
    }

    private void resetAll() {
        start.setDisable(false);
        stop.setDisable(true);
        pause.setDisable(true);
        stepin.setDisable(true);
        stepover.setDisable(true);
        tab.clearHighlight();
    }

    /**
     * disable all the buttons and change the status
     *
     * @param status the status to change the statusBar
     */
    private void disableAll(StatusBar.Status status) {
        start.setDisable(true);
        stop.setDisable(true);
        pause.setDisable(true);
        stepin.setDisable(true);
        stepover.setDisable(true);
        tab.getStatusBar().setStatus(status);
    }

    private Debugger getDebugger() {
        return tab.getProcessor().getDebugger();
    }

    private RobotID getRobotID() {
        return tab.getProcessor().getRobotID();
    }

    @Override
    public void handle(final KeyEvent event) {

        // Run
        if (KeyCombination.valueOf(FXController.hotkeys.getShortcut(Hotkeys.RUN)).match(event)) {
            start();
        }
        // Step in
        else if (KeyCombination.valueOf(FXController.hotkeys.getShortcut(Hotkeys.STEPIN)).match(event)) {
            stepIn();
        }
        // Step over
        else if (KeyCombination.valueOf(FXController.hotkeys.getShortcut(Hotkeys.STEPOVER)).match(event)) {
            stepOver();
        }
        // Pause
        else if (KeyCombination.valueOf(FXController.hotkeys.getShortcut(Hotkeys.PAUSE)).match(event)) {
            pause(true);
        }
        // Stop
        else if (KeyCombination.valueOf(FXController.hotkeys.getShortcut(Hotkeys.STOP)).match(event)) {
            stop();
        }
    }

    private void applyBreakpoints() {
        // Get all breakpoints
        List<Breakpoint> breakpoints = BreakpointPool.INSTANCE.get();

        getDebugger().setBreakpoints(breakpoints);
    }

    private void highlight(final RobotID id, final int line, final String highlightType) {

        // First we find the right tab
        Optional<FileTab> correctTab = tab.getGlobalController().getTabs().stream()
                .filter(tab -> tab instanceof RobotTab)
                .filter(tab -> ((RobotTab) tab).getProcessor().getRobotID() == id).findAny();

        if (correctTab.isPresent()) {
            // This tab is already open
            RobotTab currentTab = (RobotTab) correctTab.get();
            currentTab.getEditorPane().getEditor().clearHighlight();
            currentTab.getEditorPane().getEditor().highlightLine(line, highlightType);
            currentTab.requestFocus();
            return;
        }

        // Seems like the tab wasn't open so we open it. This has to be done in the JavaFX Thread.
        Platform.runLater(() -> {
            RobotTab newTab = tab.getGlobalController().openRobot(id.getPath());

            // Wait for the editor to load
            newTab.getEditorPane().getEditor().getOnDocumentLoaded().addListener(success ->
                    // We queue this for later execution because the tab has to display before we can scroll to the right location.
                    Platform.runLater(() -> {
                        if (success) {
                            // Highlight the tab
                            newTab.getEditorPane().getEditor().clearHighlight();
                            newTab.getEditorPane().getEditor().highlightLine(line, highlightType);
                            newTab.requestFocus();
                        }
                    })
            );
        });

    }

    @Override
    public void handle(final Throwable e) throws RobotRuntimeException {
        Logger log = LogUtil.getLogger(getRobotID());

        Throwable root = ExceptionUtils.getRootCause(e);

        LOGGER.error("Exception occurred in robot", e);
        if (root instanceof RobotRuntimeException) {
            log.error(root.getMessage());
        } else if (e instanceof RobotRuntimeException) {
            log.error(e.getMessage());
        } else if (root == null) {
            log.error("An error occurred in a robot: " + e.getMessage(), e);
        } else {
            log.error("An error occurred in a robot: " + root.getMessage(), root);
        }

        if (shouldStopOnError.isSelected()) {
            pause(true);
        }
    }
}
