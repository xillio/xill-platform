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

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Window;
import me.biesaart.utils.Log;
import nl.xillio.xill.docgen.DocumentationSearcher;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * A search bar, with the defined options and behavior.
 *
 * @author Thomas Biesaart
 * @author Pieter Dirk Soels
 */
public class HelpSearchBar extends AnchorPane {
    private static final Logger LOGGER = Log.get();
    private static int ROW_HEIGHT = 29;
    private final ListView<String> listView;
    private HelpPane helpPane;
    private final Tooltip hoverToolTip;

    private String NoResult = "No results found.";

    @FXML
    private TextField searchField;

    private final ObservableList<String> data = FXCollections.observableArrayList();
    private DocumentationSearcher searcher;

    /**
     * Default constructor.
     */
    public HelpSearchBar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HelpSearchBar.fxml"));
            loader.setClassLoader(getClass().getClassLoader());
            loader.setController(this);
            Node ui = loader.load();
            getChildren().add(ui);
        } catch (IOException e) {
            LOGGER.error("Failed to load help pane", e);
        }

        // Result list
        listView = new ListView<>(data);
        listView.setOnMouseClicked(this::onClick);
        listView.setOnKeyPressed(this::onKeyPressed);
        listView.setPrefHeight(ROW_HEIGHT);
        listView.setFixedCellSize(ROW_HEIGHT);

        // Result wrapper
        hoverToolTip = new Tooltip();
        hoverToolTip.setGraphic(listView);
        hoverToolTip.prefWidthProperty().bind(searchField.widthProperty());

        data.addListener((ListChangeListener<Object>) change -> listView.setPrefHeight((Math.min(10, data.size())) * ROW_HEIGHT + 2));

        searchField.setPromptText("Type here to start a search");
        // Listen to search changes
        searchField.textProperty().addListener(this::searchTextChanged);
        searchField.setOnKeyPressed(this::onKeyPressed);

        // Close on focus lost and when content is empty
        searchField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !searchField.getText().isEmpty()) {
                showResults();
            } else {
                hoverToolTip.hide();
            }
        });
    }

    @Override
    public void requestFocus() {
        searchField.requestFocus();
    }

    /**
     * Handles a key press.
     * Checks for an ENTER and then tries to open a page.
     *
     * @param keyEvent The key that has been pressed.
     */
    private void onKeyPressed(final KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            if (hoverToolTip.isShowing() && openSelected()) {
                return;
            } else {
                String content = searchField.getText();
                if (content.isEmpty()) {
                    helpPane.displayHome();
                } else if (content.split(" ").length == 1) {
                    tryDisplayIndexOf(content);
                }
            }
            cleanup();
        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            hoverToolTip.hide();
        } else if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.A) {
            searchField.selectAll();
        }
    }

    /**
     * Tries to display the index of a given package name.
     *
     * @param packet The name of the package we try to display.
     */
    private void tryDisplayIndexOf(final String packet) {
        helpPane.display(packet, "_index");
    }

    /**
     * Handles the clicking mechanism.
     *
     * @param mouseEvent The mouseEvent.
     */
    private void onClick(final MouseEvent mouseEvent) {
        openSelected();
    }

    /**
     * Opens the selected item.
     */
    private boolean openSelected() {
        String selected = listView.getSelectionModel().getSelectedItem();

        if (selected == null || selected.equals(NoResult)) {
            return false;
        }

        String[] parts = selected.split("\\.");
        helpPane.display(parts[0], parts[1]);
        cleanup();
        return true;
    }

    /**
     * Cleans the UI (hiding the tooltips, clearing its content etc).
     */
    void cleanup() {
        hoverToolTip.hide();
        data.clear();
        searchField.clear();
        helpPane.requestFocus();
    }

    /**
     * Set the searcher that should be used.
     *
     * @param searcher the searcher
     */
    public void setSearcher(final DocumentationSearcher searcher) {
        this.searcher = searcher;
    }

    /**
     * Set the HelpPane.
     *
     * @param help The help pane in which the search bar is embedded
     */
    public void setHelpPane(final HelpPane help) {
        helpPane = help;
    }

    // Runs the search
    private void runSearch(final String query) {
        if (searcher == null) {
            data.clear();
            return;
        }

        String[] results = searcher.search(query);
        data.clear();

        if (results.length == 0) {
            data.add(NoResult);
        } else {
            data.addAll(results);
        }
    }

    private void searchTextChanged(final Object source, final String oldValue, final String newValue) {
        if (newValue == null || newValue.isEmpty()) {
            hoverToolTip.hide();
            return;
        }

        runSearch(searchField.getText());

        showResults();
    }

    public void handleHeightChange() {
        if (hoverToolTip.isShowing()) {
            showResults();
        }
    }

    private void showResults() {
        Point2D position = searchField.localToScene(0.0, 0.0);
        Scene scene = searchField.getScene();
        Window window = scene.getWindow();

        hoverToolTip.show(
                searchField,
                position.getX() + scene.getX() + window.getX(),
                position.getY() + scene.getY() + window.getY() + searchField.getHeight());
    }
}
