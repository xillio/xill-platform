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
import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.elasticconsole.Counter;
import nl.xillio.migrationtool.elasticconsole.ESConsoleClient;
import nl.xillio.migrationtool.elasticconsole.ESConsoleClient.LogType;
import nl.xillio.migrationtool.elasticconsole.ESDataProvider;
import nl.xillio.migrationtool.elasticconsole.LogEntry;
import nl.xillio.migrationtool.elasticconsole.filters.KeywordFilter;
import nl.xillio.migrationtool.elasticconsole.filters.LogTypeFilter;
import nl.xillio.migrationtool.elasticconsole.filters.RegexFilter;
import nl.xillio.migrationtool.virtuallist.Filter;
import nl.xillio.migrationtool.virtuallist.VirtualObservableList;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.preview.Searchable;
import nl.xillio.xill.util.settings.Settings;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static nl.xillio.migrationtool.elasticconsole.ESConsoleClient.LogType.*;
import static nl.xillio.xill.util.HotkeysHandler.Hotkeys.*;

/**
 * This pane displays the console log stored in elasticsearch
 */
public class ConsolePane extends AnchorPane implements Searchable, EventHandler<KeyEvent>, RobotTabComponent {
    private static final Logger LOGGER = Log.get();
    private static final int LOG_CACHE_SIZE = 500; // We need a sufficiently large number so that the UI thread can keep up, but not so big that it becomes too heavy on memory.

    // Filters
    private final List<Filter> filters = new LinkedList<>();
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
    // Log entry lists. Master contains everything, filtered contains only selected entries.
    private VirtualObservableList<LogEntry> masterLog;
    private String searchNeedle = "";
    private boolean searchRegExp = true;
    private boolean searchCaseSensitive = true;
    private Counter count = new Counter<LogType>();
    private RobotTab tab;

    /**
     * Create and initialize a ConsolePane.
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

        // Set the cell factories
        DragSelectionCellFactory fac = new DragSelectionCellFactory();
        colLogMessage.setCellFactory(fac);
        colLogTime.setCellFactory(fac);
        colLogType.setCellFactory(fac);

        // Set the column cell value factories
        colLogTime.setCellValueFactory(new PropertyValueFactory<>("showTime"));
        colLogType.setCellValueFactory(new PropertyValueFactory<>("showType"));
        colLogMessage.setCellValueFactory(new PropertyValueFactory<>("line"));

        // Restrain column resizing
        tblConsoleOut.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Allow multiple selects
        tblConsoleOut.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Remove the default empty table text
        tblConsoleOut.setPlaceholder(new Label(""));

        // Add log filters
        addFilterListeners();
        // Assign empty list to table, data will be set later!
        tblConsoleOut.setItems(FXCollections.observableArrayList());

        // Update the filters labels
        resetLabels();
        updateLabels();

        addEventHandler(KeyEvent.KEY_PRESSED, this);

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
    public void initialize(final RobotTab tab) {
        this.tab = tab;
        if (FXController.settings.simple().getBoolean(Settings.SETTINGS_GENERAL, Settings.OPEN_BOT_WITH_CLEAN_CONSOLE)) {
            ESConsoleClient.getInstance().clearLog(getRobotID().toString());
        }

        // On robot start: update the log and scroll to the end
        getDebugger().getOnRobotStart().addListener(start -> updateLog(Scroll.TOTAL_END));

        // Fix for slower windows systems: add extra delay and then update one last time on robot stop
        getDebugger().getOnRobotStop().addListener(stop -> {
            Thread finalUpdate = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOGGER.warn("Console thread was interrupted while sleeping.", e);
                }
                updateLog(Scroll.NONE);
            }, "ConsolePane#onRobotStopRefreshConsole");
            finalUpdate.setDaemon(true);
            finalUpdate.start();
        });

        // Update the log after a log event has occurred
        ESConsoleClient.getLogEvent(getRobotID()).addListener(msg -> updateLog(Scroll.NONE));

        // Initialize the master log
        updateFilters();
        masterLog = new VirtualObservableList<>(new ESDataProvider(getRobotID().toString()), LOG_CACHE_SIZE, filters);
        tblConsoleOut.setItems(masterLog);

        // Scroll to the end of the log.
        tblConsoleOut.scrollTo(masterLog.size() - 1);
    }


    @Override
    public void handle(final KeyEvent e) {
        // Copy
        if (KeyCombination.valueOf(FXController.hotkeys.getShortcut(COPY)).match(e) && tblConsoleOut.isFocused()) {
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
        else if (KeyCombination.valueOf(FXController.hotkeys.getShortcut(CLEARCONSOLE)).match(e)) {
            clear();
            e.consume();
        }
        // Search
        else if (KeyCombination.valueOf(FXController.hotkeys.getShortcut(FIND)).match(e)) {
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
    private void buttonClearConsole() {
        // Clear the log in elasticsearch
        ESConsoleClient.getInstance().clearLog(getRobotID().toString());

        // Clear the log entries
        masterLog.clear();

        // Reset all counts
        resetLabels();

    }

    private volatile boolean isUpdating = false;

    private void updateLog(Scroll scroll) {
        if (masterLog != null && !isUpdating) {
            isUpdating = true;

            Thread thread = new Thread(() -> {

                // Short delay to prevent flooding
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    LOGGER.warn("Console thread was interrupted while sleeping.", e);
                }

                // Set filters & force update of the log table
                masterLog.setFilters(filters);

                int lastVisibleRow = getVisibleRange(tblConsoleOut)[1];
                int targetPosition = lastVisibleRow + 1 == masterLog.size() ? -1 : lastVisibleRow;
                masterLog.update(targetPosition);

                // Update counters & scroll log
                Platform.runLater(() -> {
                    // When the log starts empty, the scrollPanel does not properly sticky the
                    // bottom location of the log. Hence, we track the log size manually.
                    long initialSize = masterLog.size();

                    // Update counters
                    if (masterLog.size() > 0) {
                        count = (Counter<LogType>) masterLog.getFilterCounts();
                    }

                    // Scroll to bottom when explicitly asked to, or when log started empty.
                    if (initialSize < 10 || scroll == Scroll.END || scroll == Scroll.TOTAL_END) {
                        tblConsoleOut.scrollTo(masterLog.size() - 1);
                    } else if (scroll == Scroll.START) {
                        tblConsoleOut.scrollTo(0);
                    }

                    updateLabels();

                    isUpdating = false;
                });


            }, "ConsolePane#updateLog");
            thread.setDaemon(true);
            thread.start();
        }
    }

    /* Filters */

    private void addFilterListeners() {
        // Add listeners for the toggle filters buttons.
        tbnToggleLogsInfo.selectedProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        tbnToggleLogsDebug.selectedProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        tbnToggleLogsWarn.selectedProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        tbnToggleLogsError.selectedProperty().addListener((observable, oldValue, newValue) -> updateFilters());
    }

    private void updateFilters() {
        // Clear current filters
        filters.clear();

        // Add log type filters
        if (tbnToggleLogsInfo.isSelected()) {
            filters.add(new LogTypeFilter(INFO));
        }
        if (tbnToggleLogsDebug.isSelected()) {
            filters.add(new LogTypeFilter(DEBUG));
        }
        if (tbnToggleLogsWarn.isSelected()) {
            filters.add(new LogTypeFilter(WARN));
        }
        if (tbnToggleLogsError.isSelected()) {
            filters.add(new LogTypeFilter(ERROR));
            filters.add(new LogTypeFilter(FATAL));
        }

        // Add text filters
        if (!"".equals(searchNeedle)) {
            if (searchRegExp) {
                filters.add(new RegexFilter(searchNeedle, searchCaseSensitive));
            } else {
                filters.add(new KeywordFilter(searchNeedle, searchCaseSensitive));
            }
        }

        // Update the log
        //updateTimeline.play();
        updateLog(Scroll.NONE);
    }

    /* Filter labels */

    private void resetLabels() {
        count.clear();
        updateLabels();
    }

    private void updateLabels() {
        // Set all labels for the filters buttons
        tbnToggleLogsInfo.setText(count.get(INFO).toString());
        tbnToggleLogsDebug.setText(count.get(DEBUG).toString());
        tbnToggleLogsWarn.setText(count.get(WARN).toString());
        tbnToggleLogsError.setText(Integer.toString(count.get(ERROR) + count.get(FATAL)));
    }


    @Override
    public void searchPattern(final String pattern, final boolean caseSensitive) {
        searchNeedle = pattern;
        searchRegExp = true;
        searchCaseSensitive = caseSensitive;

        updateFilters();

        // Select the first line.
        select(0);
    }

    @Override
    public void search(final String needle, final boolean caseSensitive) {
        searchNeedle = needle;
        searchRegExp = false;
        searchCaseSensitive = caseSensitive;

        updateFilters();

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
        searchNeedle = "";
        updateFilters(); // show all lines (without searching affected)
        tblConsoleOut.getSelectionModel().clearSelection();

        updateFilters();
    }

    /* Drag selection */

    private RobotID getRobotID() {
        return tab.getProcessor().getRobotID();
    }

    private Debugger getDebugger() {
        return tab.getProcessor().getDebugger();
    }

    public enum Scroll {
        NONE, START, END, TOTAL_END, CLEAR
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

        DragSelectionCell() {
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
            text.wrappingWidthProperty().bind(widthProperty());
            text.textProperty().bind(itemProperty());

            setGraphic(text);

            setMaxHeight(text.getLayoutBounds().getHeight());
            setAlignment(Pos.TOP_LEFT);
        }

        /**
         * Create the contents of this cell as {@link TextArea}, useful for selecting text partially
         */
        public void createTextArea() {
            TextInputControl textArea;
            if (getItem().indexOf('\n') > 0) {
                TextArea ta = new TextArea();
                ta.setWrapText(true);
                textArea = ta;
            } else {
                textArea = new TextField();
            }

            textArea.textProperty().bind(itemProperty());
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

        DragDetectedEventHandler(final TableCell<LogEntry, String> tableCell) {
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

        DragEnteredEventHandler(final TableCell<LogEntry, String> tableCell) {
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

        DoubleClickEventHandler(final DragSelectionCell tableCell) {
            this.tableCell = tableCell;
        }

        @Override
        public void handle(MouseEvent event) {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                tableCell.createTextArea();
            }
        }
    }

    private int[] getVisibleRange(TableView table) {
        TableViewSkin<?> skin = (TableViewSkin) table.getSkin();
        if (skin == null) {
            return new int[] {0, 0};
        }
        VirtualFlow<?> flow = (VirtualFlow) skin.getChildren().get(1);
        int indexFirst;
        int indexLast;
        if (flow != null && flow.getFirstVisibleCellWithinViewPort() != null
                && flow.getLastVisibleCellWithinViewPort() != null) {
            indexFirst = flow.getFirstVisibleCellWithinViewPort().getIndex();
            if (indexFirst >= table.getItems().size())
                indexFirst = table.getItems().size() - 1;
            indexLast = flow.getLastVisibleCellWithinViewPort().getIndex();
            if (indexLast >= table.getItems().size())
                indexLast = table.getItems().size() - 1;
        } else {
            indexFirst = 0;
            indexLast = 0;
        }
        return new int[] {indexFirst, indexLast};
    }
}
