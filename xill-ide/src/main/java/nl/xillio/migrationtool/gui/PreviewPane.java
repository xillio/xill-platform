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
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.Pair;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.gui.searching.PreviewSearch;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.preview.HtmlPreview;
import nl.xillio.xill.api.preview.TextPreview;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * This pane can show a visual representation of the Xill IDE Variable
 * classes.
 */
public class PreviewPane extends AnchorPane implements FileTabComponent {
    @FXML
    private AnchorPane apnPreviewPane;
    @FXML
    private SearchBar apnPreviewSearchBar;
    @FXML
    private ToggleButton tbnPreviewSearch;
    private final TextArea textView = new TextArea();
    private PreviewSearch previewSearch;

    private static final Logger LOGGER = Log.get();

    /**
     * Create a new PreviewPane
     */
    public PreviewPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PreviewPane.fxml"));
            loader.setClassLoader(getClass().getClassLoader());
            loader.setController(this);
            getChildren().add(loader.load());
        } catch (IOException e) {
            LOGGER.error("Error loading Preview pane: " + e.getMessage(), e);
        }

        previewSearch = new PreviewSearch(apnPreviewPane);
        apnPreviewSearchBar.setSearchable(previewSearch);
        apnPreviewSearchBar.setButton(tbnPreviewSearch, 1);

        AnchorPane.setBottomAnchor(textView, 0.0);
        AnchorPane.setTopAnchor(textView, 0.0);
        AnchorPane.setLeftAnchor(textView, 0.0);
        AnchorPane.setRightAnchor(textView, 0.0);
    }

    public void preview(final ObservableVariable observableVariable) {
        apnPreviewSearchBar.reset(false);
        previewSearch.clearSearch();

        MetaExpression value = observableVariable.getExpression();

        apnPreviewPane.getChildren().clear();
        Node node = getPreview(value, apnPreviewSearchBar);
        if (node == null) {
            node = buildTree(value);
        }

        if (node instanceof Text) {
            textView.setText(((Text) node).getText());
            apnPreviewPane.getChildren().add(textView);
        } else {
            apnPreviewPane.getChildren().add(node);
        }

        apnPreviewSearchBar.refresh();
    }

    private void clear() {
        Platform.runLater(() -> {
            apnPreviewSearchBar.reset(true);
            apnPreviewSearchBar.close(true);
            apnPreviewPane.getChildren().clear();
        });
    }

    private Node getPreview(final MetaExpression expression, final SearchBar searchBar) {
        try {
            // First allow the expression to provide a preview
            if (expression.hasMeta(HtmlPreview.class)) {
                HtmlPreview preview = expression.getMeta(HtmlPreview.class);
                return getTextPreview(preview.getHtmlPreview());
            }
            if (expression.hasMeta(TextPreview.class)) {
                TextPreview preview = expression.getMeta(TextPreview.class);
                return getTextPreview(preview.getTextPreview());
            }

            if (expression.getType() == ExpressionDataType.ATOMIC) {
                return getTextPreview(expression.getStringValue());
            }
        } catch (IllegalStateException e) {
            // CTC-1892 - Catch rare expression-already-closed exception
            return getTextPreview(e.getMessage());
        }

        return null;
    }

    private Node getTextPreview(String text) {
        Text preview = new Text(text);
        Tooltip tooltip = new Tooltip(WordUtils.wrap(text, 200, "\n", true));
        tooltip.setWrapText(true);

        Tooltip.install(preview, tooltip);
        return preview;
    }

    @SuppressWarnings("unchecked")
    private TreeTableView<Pair<String, MetaExpression>> buildTree(final MetaExpression rootValue) {
        /* Root node should be invisible and already expanded.
        This ensures you can see the top level nodes and lazily expand them. */
        PreviewTreeItem root = new PreviewTreeItem("ROOT", rootValue);
        root.setExpanded(true);
        TreeTableView<Pair<String, MetaExpression>> tableView = new TreeTableView<>(root);
        tableView.setShowRoot(false);
        // Create mapping columns
        TreeTableColumn<Pair<String, MetaExpression>, String> keyColumn = new TreeTableColumn<>();
        TreeTableColumn<Pair<String, MetaExpression>, MetaExpression> valueColumn = new TreeTableColumn<>();
        keyColumn.setText("Key");
        valueColumn.setText("Value");
        // Yield variable preview properties
        keyColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getValue().getKey()));
        valueColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getValue().getValue()));

        FocusCellFactory fac = new FocusCellFactory();
        valueColumn.setCellFactory(fac);
        // Apply preview factories
        tableView.getColumns().setAll(
                keyColumn, valueColumn
        );
        tableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        // Setup corner layout constraints
        AnchorPane.setBottomAnchor(tableView, 0.0);
        AnchorPane.setTopAnchor(tableView, 0.0);
        AnchorPane.setRightAnchor(tableView, 0.0);
        AnchorPane.setLeftAnchor(tableView, 0.0);
        return tableView;
    }

    /**
     * Open the search bar
     */
    public void openSearch() {
        apnPreviewSearchBar.open(1);
        apnPreviewSearchBar.requestFocus();
    }

    /**
     * Assign current tab and clear panel once stopped.
     *
     * @param tab The current robot tab.
     */
    @Override
    public void initialize(final FileTab tab) {
        ((RobotTab) tab).getProcessor().getDebugger().getOnRobotStop().addListener(action -> clear());
    }

    private class FocusCellFactory implements Callback<TreeTableColumn<Pair<String, MetaExpression>, MetaExpression>, TreeTableCell<Pair<String, MetaExpression>, MetaExpression>> {
        @Override
        public TreeTableCell<Pair<String, MetaExpression>, MetaExpression> call(final TreeTableColumn<Pair<String, MetaExpression>, MetaExpression> col) {
            return new PreviewCellItem();
        }
    }
}
