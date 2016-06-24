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

import com.sun.javafx.scene.control.behavior.CellBehaviorBase;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.Duration;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.elasticconsole.Counter;
import nl.xillio.migrationtool.elasticconsole.ESConsoleClient;
import nl.xillio.migrationtool.elasticconsole.ESConsoleClient.LogType;
import nl.xillio.migrationtool.elasticconsole.LogEntry;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.preview.Searchable;
import nl.xillio.xill.util.HotkeysHandler.Hotkeys;
import nl.xillio.xill.util.settings.Settings;
import org.slf4j.Logger;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This pane displays the console log stored in elasticsearch
 */
public class ConsolePane extends AnchorPane implements Searchable, EventHandler<KeyEvent>, RobotTabComponent {

    private static final Logger LOGGER = Log.get();
    // Log entry lists. Master contains everything, filtered contains only selected entries.
    private final ObservableList<LogEntry> masterLog = FXCollections.observableArrayList();
    private final FilteredList<LogEntry> filteredLog = new FilteredList<>(masterLog, e -> true);
    // Updating the console
    private final Timeline updateTimeline;
    private final int maxEntries = 1000; // the number of lines in one "page"
    private final Timeline sliderTimeline; // update cycle for slider changes
    // Time format
    private final DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // Filters
    private final Map<LogType, Boolean> filters = new HashMap<>(LogType.values().length);
    private final Map<LogType, Integer> count = new Counter<>();
    @FXML
    private SearchBar apnConsoleSearchBar;
    @FXML
    private TableView<LogEntry> tblConsoleOut;
    @FXML
    private TableColumn<LogEntry, String> colLogTime;
    @FXML
    private TableColumn<LogEntry, String> colLogType;
    @FXML
    private TableColumn<LogEntry, String> colLogMessage;
    @FXML
    private ToggleButton tbnToggleLogsInfo;
    @FXML
    private ToggleButton tbnToggleLogsDebug;

    // private Robot robot;
    @FXML
    private ToggleButton tbnToggleLogsWarn;
    @FXML
    private ToggleButton tbnToggleLogsError;
    @FXML
    private ToggleButton tbnConsoleSearch;
    @FXML
    private Label tbnLogsCount;
    @FXML
    private Button btnNavigateBack;
    @FXML
    private Button btnNavigateForward;
    @FXML
    private Slider sldNavigation;
    private int startEntry = 0; // first entry to show
    private int entriesCount = 0; // number of total entries in ES
    private boolean updateSlider = true; // if changes on slider value has to invoke updateLog()
    private boolean sliderChanged = false; // if slider value has changed from outside - because of some user activity
    private RobotTab tab;
    /* Searching */
    private boolean searchRegExp = false;
    private String searchNeedle = "";

    /**
     * Creates and initialize a ConsolePane.
     */
    public ConsolePane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConsolePane.fxml"));
            loader.setClassLoader(getClass().getClassLoader());
            loader.setController(this);
            Node ui = loader.load();
            getChildren().add(ui);
        } catch (IOException e) {
            LOGGER.error("Error loading console pane: " + e.getMessage(), e);
        }

        // Search bar
        apnConsoleSearchBar.setSearchable(this);
        apnConsoleSearchBar.setButton(tbnConsoleSearch, 1);

        // Console updater timeline
        updateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateLog(Scroll.TOTALEND, false)));

        // Log updater - when some slider changes happen
        sliderTimeline = new Timeline(new KeyFrame(Duration.millis(1000), e -> {
            if (this.sliderChanged) {
                Platform.runLater(() -> this.updateLog(Scroll.START, true));
            }
        }));
        sliderTimeline.setCycleCount(Timeline.INDEFINITE);
        sliderTimeline.play();

        // Set the cell factories
        DragSelectionCellFactory fac = new DragSelectionCellFactory();
        colLogMessage.setCellFactory(fac);
        colLogTime.setCellFactory(fac);
        colLogType.setCellFactory(fac);

        // Set the column cell value factories
        colLogTime.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("showTime"));
        colLogType.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("showType"));
        colLogMessage.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("line"));

        // Restrain column resizing
        tblConsoleOut.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Allow multiple selects
        tblConsoleOut.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Remove the default empty table text
        tblConsoleOut.setPlaceholder(new Label(""));

        // Add log filters
        addFilterListeners();
        tblConsoleOut.setItems(filteredLog);

        // Update the filter labels
        resetLabels();
        updateLabels();

        addEventHandler(KeyEvent.KEY_PRESSED, this);

        // Listener for slider value changes
        sldNavigation.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (this.updateSlider) {
                this.startEntry = (this.entriesCount * newValue.intValue()) / 100;
                this.sliderChanged = true;
            }
        });

    }

    private static void performSelection(final TableView<LogEntry> table, final int index) {
        // Get the table anchor
        @SuppressWarnings("unchecked")
        final TablePositionBase<TableColumn<LogEntry, String>> anchor = CellBehaviorBase.getAnchor(table, table.getFocusModel().getFocusedCell());

        // Get the min and max row index and select the rows in that range
        int minRowIndex = Math.min(anchor.getRow(), index);
        int maxRowIndex = Math.max(anchor.getRow(), index);
        table.getSelectionModel().selectRange(minRowIndex, maxRowIndex + 1);

        // Set the focus
        table.getFocusModel().focus(index);
    }

    @Override
    public void handle(final KeyEvent e) {
        // Copy
        if (KeyCombination.valueOf(FXController.hotkeys.getShortcut(Hotkeys.COPY)).match(e) && tblConsoleOut.isFocused()) {
            // Get all selected entries
            StringBuilder text = new StringBuilder();
            ObservableList<LogEntry> selected = tblConsoleOut.getSelectionModel().getSelectedItems();

            // Append the text from all entries
            selected.forEach(entry -> text.append(entry.getLine()).append('\n'));

            // Set the clipboard content
            final ClipboardContent content = new ClipboardContent();
            content.putString(text.toString());
            Clipboard.getSystemClipboard().setContent(content);

            e.consume();
        }
        // Clear
        else if (KeyCombination.valueOf(FXController.hotkeys.getShortcut(Hotkeys.CLEARCONSOLE)).match(e)) {
            clear();
            e.consume();
        }
        // Search
        else if (KeyCombination.valueOf(FXController.hotkeys.getShortcut(Hotkeys.FIND)).match(e)) {
            apnConsoleSearchBar.open(1);
            e.consume();
        }
    }

    /**
     * Clears the console.
     */
    public void clear() {
        buttonClearConsole();
    }

    @FXML
    private void buttonNavigateBack() {
        this.startEntry -= this.maxEntries;
        if (this.startEntry < 0) {
            this.startEntry = 0;
        }
        this.updateLog(Scroll.END, false);
    }

    @FXML
    private void buttonNavigateForward() {
        this.startEntry += this.maxEntries;
        this.updateLog(Scroll.START, false);
    }

    @FXML
    private void buttonClearConsole() {
        // Clear the log in elasticsearch
        ESConsoleClient.getInstance().clearLog(getRobotID().toString());

        // Clear the log entries
        masterLog.clear();

        // Reset all counts
        resetLabels();
        updateLog(Scroll.CLEAR, false);
    }

    private void updateLog(Scroll scroll, boolean fromSlider) {
        Platform.runLater(() -> {
            // Clear the log
            masterLog.clear();

            // Reset the filter counts
            resetLabels();

            if (scroll == Scroll.CLEAR) {
                // this explicit variant is here because if you click Clear then the ES starts to remove all items,
                // but when runtime come here, ES still can have some values "not deleted yet" and
                // because the clear operation on ES is asynchronous
                this.startEntry = 0;
                this.entriesCount = 0;
                tbnLogsCount.setText("0-0/0");
                updateLabels();
                return;
            }

            ESConsoleClient.SearchFilter filter = ESConsoleClient.getInstance().createSearchFilter(searchNeedle, this.searchRegExp, this.filters);

            this.entriesCount = ESConsoleClient.getInstance().countFilteredEntries(getRobotID().toString(), filter);

            int lastEntry = this.startEntry + this.maxEntries - 1;
            if ((this.entriesCount <= lastEntry) || (scroll == Scroll.TOTALEND)) {
                lastEntry = this.entriesCount - 1;
                this.startEntry = lastEntry - this.maxEntries + 1;
                if (this.startEntry < 0) {
                    this.startEntry = 0;
                }
            }

            if (!fromSlider) {
                this.updateSlider = false;
                if (this.entriesCount == 0) {
                    this.sldNavigation.setValue(0);
                } else {
                    this.sldNavigation.setValue((this.startEntry * 100) / this.entriesCount);
                }
                this.updateSlider = true;
            }

            if (this.entriesCount == 0) {
                tbnLogsCount.setText("0-0/0");
            } else {
                tbnLogsCount.setText(String.format("%1$d-%2$d/%3$d", this.startEntry + 1, lastEntry + 1, this.entriesCount));
            }

            List<Map<String, Object>> entries = ESConsoleClient.getInstance().getFilteredEntries(
                    getRobotID().toString(), this.startEntry, lastEntry, filter);

            for (Map<String, Object> entry : entries) {
                // Get all properties
                String time = timeFormat.format(new Date((long) entry.get("timestamp")));
                LogType type = LogType.valueOf(entry.get("type").toString().toUpperCase());
                addTableEntry(time, type, getMessageText(entry.get("message")), false);
            }

            // Do scroll
            if ((scroll == Scroll.END) || (scroll == Scroll.TOTALEND)) {
                tblConsoleOut.scrollTo(tblConsoleOut.getItems().size() - 1);
            } else if (scroll == Scroll.START) {
                tblConsoleOut.scrollTo(0);
            }

            updateLabels();
            this.sliderChanged = false;
        });
    }

    private String getMessageText(final Object message) {
        if (message == null) {// This is protection against when message is null, otherwise the console would stop working..
            LOGGER.error("Null message passed to console!");
            return "null";
        } else {
            return message.toString();
        }
    }

    private void addTableEntry(final String time, final LogType type, final String line, final boolean newLine) {
        // Add the entry to the master log and add to the count
        masterLog.add(new LogEntry(time, type, line, newLine));
        if (!newLine) {
            count.put(type, count.get(type) + 1);
        }
    }

    private void addFilterListeners() {
        // Add default filters
        filters.put(LogType.INFO, tbnToggleLogsInfo.isSelected());
        filters.put(LogType.DEBUG, tbnToggleLogsDebug.isSelected());
        filters.put(LogType.WARN, tbnToggleLogsWarn.isSelected());
        filters.put(LogType.ERROR, tbnToggleLogsError.isSelected());
        filters.put(LogType.FATAL, tbnToggleLogsError.isSelected());

        // Add listeners for the toggle filter buttons
        tbnToggleLogsInfo.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue) {
                filters.put(LogType.INFO, newValue);
                updateFilters();
            }
        });
        tbnToggleLogsDebug.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue) {
                filters.put(LogType.DEBUG, newValue);
                updateFilters();
            }
        });
        tbnToggleLogsWarn.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue) {
                filters.put(LogType.WARN, newValue);
                updateFilters();
            }
        });
        tbnToggleLogsError.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue) {
                filters.put(LogType.ERROR, newValue);
                filters.put(LogType.FATAL, newValue);
                updateFilters();
            }
        });
    }

    private void updateFilters() {
        this.updateLog(Scroll.NONE, false);
    }

    private void resetLabels() {
        count.clear();
    }

    private void updateLabels() {
        // Set all labels for the filter buttons
        tbnToggleLogsInfo.setText(count.get(LogType.INFO).toString());
        tbnToggleLogsDebug.setText(count.get(LogType.DEBUG).toString());
        tbnToggleLogsWarn.setText(count.get(LogType.WARN).toString());
        tbnToggleLogsError.setText(Integer.toString(count.get(LogType.ERROR) + count.get(LogType.FATAL)));
    }

    @Override
    public void searchPattern(final String pattern, final boolean caseSensitive) {
        this.searchNeedle = pattern;
        this.searchRegExp = true;
        this.startEntry = 0;
        this.updateLog(Scroll.START, false);

        // Select the first line.
        select(0);
    }

    @Override
    public void search(final String needle, final boolean caseSensitive) {
        this.searchNeedle = needle;
        this.searchRegExp = false;
        this.startEntry = 0;
        this.updateLog(Scroll.START, false);

        // Select the first line.
        Platform.runLater(() -> select(0));
    }

    @Override
    public int getOccurrences() {
        return tblConsoleOut.getItems().size();
    }

    @Override
    public void findNext(int next) {
        select(next);
    }

    @Override
    public void findPrevious(int previous) {
        select(previous);
    }

    private void select(int line) {
        // Clear and select, scroll to the line
        tblConsoleOut.getSelectionModel().clearAndSelect(line);
        tblConsoleOut.scrollTo(line);
    }

    @Override
    public void clearSearch() {
        this.searchNeedle = "";
        this.updateFilters(); // show all lines (without searching affected)
        tblConsoleOut.getSelectionModel().clearSelection();
    }

    /* Drag selection */

    @Override
    public void initialize(final RobotTab tab) {
        this.tab = tab;
        if (new Boolean(FXController.settings.simple().get(Settings.SETTINGS_GENERAL, Settings.OpenBotWithCleanConsole)).booleanValue()) {
            ESConsoleClient.getInstance().clearLog(getRobotID().toString());
        }
        getDebugger().getOnRobotStart().addListener(start -> updateLog(Scroll.TOTALEND, false));
        ESConsoleClient.getLogEvent(getRobotID()).addListener(msg -> updateTimeline.play());
        updateTimeline.play();
    }

    private RobotID getRobotID() {
        return tab.getProcessor().getRobotID();
    }

    private Debugger getDebugger() {
        return tab.getProcessor().getDebugger();
    }

    public enum Scroll {
        NONE, START, END, TOTALEND, CLEAR
    }

    /**
     * The cell factory which creates DragSelectionCells for the console table.
     */
    private class DragSelectionCellFactory implements Callback<TableColumn<LogEntry, String>, TableCell<LogEntry, String>> {
        @Override
        public TableCell<LogEntry, String> call(final TableColumn<LogEntry, String> col) {
            return new DragSelectionCell();
        }
    }

    /**
     * A selection cell which can be selected by dragging over it with the mouse.
     */
    private class DragSelectionCell extends TableCell<LogEntry, String> {

        public DragSelectionCell() {
            // Set event handlers
            setOnDragDetected(new DragDetectedEventHandler(this));
            setOnMouseDragEntered(new DragEnteredEventHandler(this));

            // show a TextArea on double click, for selecting text partially
            setOnMouseClicked(new DoubleClickEventHandler(this));

            createText();

            // Update width
            Platform.runLater(() -> setWidth(getWidth() + 0.1));
        }

        /**
         * Create the contents of this cell as {@link Text}
         */
        public void createText() {
            Text text = new Text();
            text.wrappingWidthProperty().bind(this.widthProperty());
            text.textProperty().bind(this.itemProperty());

            setGraphic(text);
        }

        /**
         * Create the contents of this cell as {@link TextArea}, useful for selecting text partially
         */
        public void createTextArea() {
            TextArea textArea = new TextArea();
            textArea.setWrapText(true);
            textArea.textProperty().bind(this.itemProperty());
            // Keep the same height as before
            textArea.setPrefHeight(getHeight());

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

    private class DragDetectedEventHandler implements EventHandler<MouseEvent> {
        private final TableCell<LogEntry, String> tableCell;

        public DragDetectedEventHandler(final TableCell<LogEntry, String> tableCell) {
            this.tableCell = tableCell;
        }

        @Override
        public void handle(final MouseEvent event) {
            // Start dragging
            tableCell.startFullDrag();
        }
    }

    private class DragEnteredEventHandler implements EventHandler<MouseDragEvent> {
        private final TableCell<LogEntry, String> tableCell;

        public DragEnteredEventHandler(final TableCell<LogEntry, String> tableCell) {
            this.tableCell = tableCell;
        }

        @Override
        public void handle(final MouseDragEvent event) {
            // When the mouse drag enters the cell, perform a selection
            performSelection(tableCell.getTableView(), tableCell.getIndex());
        }
    }

    private class DoubleClickEventHandler implements EventHandler<MouseEvent> {
        private final DragSelectionCell tableCell;

        public DoubleClickEventHandler(final DragSelectionCell tableCell) {
            this.tableCell = tableCell;
        }

        @Override
        public void handle(MouseEvent event) {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                tableCell.createTextArea();
            }
        }
    }
}
