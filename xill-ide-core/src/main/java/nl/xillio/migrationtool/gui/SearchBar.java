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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import me.biesaart.utils.Log;
import nl.xillio.events.Event;
import nl.xillio.events.EventHost;
import nl.xillio.xill.api.preview.Searchable;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * A search bar, with the defined options and behavior.
 */
public class SearchBar extends AnchorPane implements EventHandler<KeyEvent> {

    private static final Logger LOGGER = Log.get();

    private Searchable searchable;
    private String currentSearch = "";
    private boolean enabledSearchAsYouType = false;
    private final EventHost<Boolean> closeEvent = new EventHost<>();
    private boolean isOpen = false;
    /**
     * The current highlighted occurrence
     */
    private Node storedParent;
    protected int currentOccurrence = 0;

    // Nodes
    @FXML
    protected TextField tfEditorSearchQuery;
    @FXML
    private Label lblEditorSearchIndex;
    @FXML
    private Label lblEditorSearchCount;
    @FXML
    private ToggleButton tbnEditorRegexSearch;
    @FXML
    private ToggleButton tbnEditorCaseSensitive;
    private ToggleButton toggleButton;

    /**
     * Default constructor. The bar won't do anything until {@link #setSearchable(Searchable)} is called.
     */
    public SearchBar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SearchBar.fxml"));
            loader.setClassLoader(getClass().getClassLoader());
            loader.setController(this);
            Node ui = loader.load();
            getChildren().add(ui);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        reset(true);
        enableSearchAsYouType();

        this.addEventHandler(KeyEvent.KEY_PRESSED, this);
    }

    /**
     * Resets the search bar: resets the counts and optionally the query.
     *
     * @param clearQuery whether the query should be erased
     */
    public void reset(final boolean clearQuery) {
        if (clearQuery) {
            tfEditorSearchQuery.clear();
        }

        currentOccurrence = 0;
        // Reset the count and index labels.
        Platform.runLater(() -> {
            lblEditorSearchCount.setText("0");
            lblEditorSearchIndex.setText("0");
        });
    }

    /**
     * Set the toggle button for this searchbar
     *
     * @param toggleButton The search button
     * @param id           The index to pass to {@link SearchBar#open(int)}
     */
    public void setButton(final ToggleButton toggleButton, final int id) {
        this.toggleButton = toggleButton;

        toggleButton.selectedProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal) {
                        open(id);
                    } else {
                        close(true);
                    }
                });
    }

    private void enableSearchAsYouType() {
        enabledSearchAsYouType = true;
        tfEditorSearchQuery.textProperty().addListener((arg0, oldvalue, newvalue) -> runSearch(newvalue));
    }

    private void runSearch(final String query) {
        // A new search query. Clear old data
        reset(false);

        currentSearch = query;

        // Check if the query is empty
        if (query.isEmpty()) {
            searchable.clearSearch();
            return;
        }

        if (isRegexEnabled()) {
            searchable.searchPattern(query, isCaseSensitive());
        } else {
            searchable.search(query, isCaseSensitive());
        }

        // Update the labels.
        updateLabels();
    }

    /**
     * Run search again on current state of search (i.e. use current search settings)
     * Usually used right after the source data of search has been changed - so it will do search with current settings on a new data
     */
    public void refresh() {
        String query = tfEditorSearchQuery.getText();
        if (!query.isEmpty()) {
            Platform.runLater(() -> runSearch(query));
        }
    }

    /**
     * Returns whether regex are enabled.
     *
     * @return whether regex are enabled
     */
    public boolean isRegexEnabled() {
        return tbnEditorRegexSearch.selectedProperty().get();
    }

    /**
     * Returns whether the search should be case sensitive.
     *
     * @return whether the search should be case sensitive
     */
    public boolean isCaseSensitive() {
        return tbnEditorCaseSensitive.selectedProperty().get();
    }

    private String getSearchQuery() {
        return tfEditorSearchQuery.getText();
    }

    /**
     * Attaches a searchable item to this bar.
     *
     * @param searchable the item to attach to this bar
     */
    public void setSearchable(final Searchable searchable) {
        this.searchable = searchable;
        // Close on load
        close(false);
    }

    /**
     * Returns the searchable attached to this bar.
     *
     * @return the searchable attached to this bar
     */
    public Searchable getSearchable() {
        return searchable;
    }

    private void updateLabels() {
        // Update the count and index labels.
        Platform.runLater(() -> {
            lblEditorSearchCount.setText(String.valueOf(searchable.getOccurrences()));
            lblEditorSearchIndex.setText(String.valueOf(searchable.getOccurrences() == 0 ? 0 : currentOccurrence + 1));
        });
    }

    /**
     * Update the current occurrence to the next of previous.
     *
     * @param next Whether the next or previous occurrence was selected.
     */
    private void updateOccurrence(final boolean next) {
        if (!currentSearch.isEmpty()) {
            // Set the current occurrence.
            currentOccurrence += next ? 1 : -1;
            currentOccurrence = searchable.getOccurrences() == 0 ? -1 : currentOccurrence % searchable.getOccurrences();
            if (currentOccurrence < 0)
                currentOccurrence += searchable.getOccurrences();

            // Find the next/previous occurrence.
            if (next)
                searchable.findNext(currentOccurrence);
            else
                searchable.findPrevious(currentOccurrence);

            updateLabels();
        } else
            reset(false);
    }

    /**
     * Set the current occurrence.
     *
     * @param current The index of the current occurrance.
     */
    public void setCurrentOccurrence(int current) {
        currentOccurrence = current;
        updateLabels();
    }

    ///////////////////// Controls /////////////////////

    @FXML
    private void nextButtonPressed(final ActionEvent actionEvent) {
        updateOccurrence(true);
    }

    @FXML
    private void previousButtonPressed() {
        updateOccurrence(false);
    }

    @FXML
    private void caseButtonPressed() {
        runSearch(getSearchQuery());
    }

    @FXML
    private void regexButtonPressed() {
        runSearch(getSearchQuery());
    }

    @FXML
    private void closeButtonPressed() {
        close(true);
    }

    ///////////////////// Open and close /////////////////////

    /**
     * Hide the searchbar and remove it from the flow.
     *
     * @param clear if the search bar content and any highlights should be cleared
     */
    public void close(final boolean clear) {
        // Clear the search
        if (clear) {
            searchable.clearSearch();
        }

        // Remove this from its parent
        if (getParent() != null) {
            storedParent = getParent();
            if (storedParent instanceof Pane) {
                ((Pane) storedParent).getChildren().remove(this);
            }
        }

        // Unselect the toggle button
        if (toggleButton != null) {
            toggleButton.setSelected(false);

            if (this.isOpen) {
                closeEvent.invoke(true);
                this.isOpen = false;
            }
        }
    }

    /**
     * @return true if searchbar is currently open, false if it's closed
     */
    public boolean isOpen() {
        return this.isOpen;
    }

    /**
     * Open the searchbar as the first child of it's parent.
     */
    public void open() {
        open(0);
    }

    /**
     * Open up the search bar as a child of it's previous parent
     *
     * @param index the index in the list of it's parent's children.
     */
    public void open(final int index) {
        // Check if we have a parent
        if (storedParent != null && getParent() == null) {
            close(false);

            // Add this as a child
            if (storedParent instanceof Pane) {
                ((Pane) storedParent).getChildren().add(index, this);
            }

            requestFocus();
            this.isOpen = true;
        }

        // Select the toggle button
        if (toggleButton != null) {
            toggleButton.setSelected(true);
        }
    }

    @Override
    public void requestFocus() {
        tfEditorSearchQuery.requestFocus();
        tfEditorSearchQuery.selectAll();
    }

    @Override
    public void handle(final KeyEvent keyEvent) {
        if (isChildFocused() && !keyEvent.isConsumed()) {
            switch (keyEvent.getCode()) {
                case ENTER:
                    if (currentSearch != null) {
                        enterPressed();
                        keyEvent.consume();
                    }
                    break;
                case ESCAPE:
                    close(false);
                    keyEvent.consume();
                    break;
            }
        }
    }

    /**
     * Set the text for the search query.
     *
     * @param text The text to set.
     */
    public void setSearchText(String text) {
        tfEditorSearchQuery.setText(text);
        tfEditorSearchQuery.positionCaret(text.length());
    }

    private void enterPressed() {
        if (enabledSearchAsYouType) {
            nextButtonPressed(null);
        } else {
            runSearch(currentSearch);
        }
    }

    /**
     * @return event that is invoked when the search bar is closed
     */
    public Event<Boolean> getOnClose() {
        return closeEvent.getEvent();
    }

    protected boolean isChildFocused() {
        Node child = this.getScene().getFocusOwner();

        if (child == null) {
            return false;
        }

        // Traverse upwards until we find this, or null.
        Parent curr = child.getParent();
        while (curr != null) {
            if (curr == this) {
                return true;
            }
            curr = curr.getParent();
        }

        return false;
    }
}
