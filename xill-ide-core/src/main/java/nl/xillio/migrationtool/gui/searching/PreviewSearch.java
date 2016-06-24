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
package nl.xillio.migrationtool.gui.searching;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import me.biesaart.utils.Log;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.preview.Searchable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This class implements searching mechanism for PreviewPane
 * It can search both in TextArea and treetable
 *
 * @author Zbynek Hochmann
 */
public class PreviewSearch implements Searchable {

    private static final Logger LOGGER = Log.get();

    private class SearchTextOccurrence {
        private final int start;
        private final String match;
        private final int end;

        public SearchTextOccurrence(final int start, final String match) {
            this.start = start;
            this.match = match;
            end = start + match.length();
        }

        public int getStart() {
            return start;
        }

        public String getMatch() {
            return match;
        }

        public int getEnd() {
            return end;
        }
    }

    private class SearchTreeOccurrence {
        public SearchTreeOccurrence(TreeItem<Pair<String, MetaExpression>> item) {
            this.item = item;
        }

        public TreeItem<Pair<String, MetaExpression>> getItem() {
            return item;
        }

        private TreeItem<Pair<String, MetaExpression>> item;
    }

    private AnchorPane apnPreviewPane;
    private final List<Object> occurrences = new ArrayList<>(); // List of found occurrences
    private Pattern regexPattern; // Regexp pattern used while searching the needle
    private TreeTableView<Pair<String, MetaExpression>> tableView; // treetable instance (null if does not exist)
    private TextArea text; // textarea instance (null if does not exist)

    public PreviewSearch(final AnchorPane apnPreviewPane) {
        this.apnPreviewPane = apnPreviewPane;
    }

    @SuppressWarnings("squid:S1166") // PatternSyntaxException is handled correctly
    @Override
    public void searchPattern(String pattern, boolean caseSensitive) {
        // Clear selection
        clearSearch();

        // Try to compile the pattern, get the matcher
        regexPattern = null;
        try {
            regexPattern = caseSensitive ? Pattern.compile(pattern) : Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            return; // This return in catch (i.e. exception handling missing) is intentionally - if not valid regexp pattern is passed from user, it will do nothing
        }

        occurrences.clear();

        if (apnPreviewPane.getChildren().size() == 0) { // PreviewPane does not contain any preview item at the moment
            return;
        }

        Node node = apnPreviewPane.getChildren().get(0);
        if (node instanceof TextArea) {
            text = (TextArea) node;
            tableView = null;
            searchText(); // Search in textarea
        } else {
            tableView = getTreeTableView(node);
            text = null;
            searchTreeIterate(tableView.getRoot()); // Search in treetable
        }

        Platform.runLater(() -> select(0));
    }

    @SuppressWarnings("unchecked")
    private TreeTableView<Pair<String, MetaExpression>> getTreeTableView(final Node node) {
        return (TreeTableView<Pair<String, MetaExpression>>) node;
    }

    @Override
    public void search(String needle, boolean caseSensitive) {
        String pattern = Pattern.quote(needle);
        searchPattern(pattern, caseSensitive);
    }

    @Override
    public int getOccurrences() {
        return occurrences.size();
    }

    @Override
    public void findNext(int next) {
        select(next);
    }

    @Override
    public void findPrevious(int previous) {
        select(previous);
    }

    @Override
    public void clearSearch() {
        occurrences.clear();
    }

    private void select(final int occurrence) {
        if (occurrences.size() == 0) {
            return;
        }

        if (text == null) {
            selectTreeItem(occurrence);
        } else {
            selectText(occurrence);
        }
    }

    private void selectText(final int occurrence) {
        if (occurrence >= 0 && occurrence < occurrences.size()) {
            SearchTextOccurrence element = (SearchTextOccurrence) occurrences.get(occurrence);
            text.selectRange(element.getStart(), element.getEnd());

        }
    }

    private void selectTreeItem(final int occurrence) {
        SearchTreeOccurrence treeOccurrence = (SearchTreeOccurrence) occurrences.get(occurrence);

        Node node = apnPreviewPane.getChildren().get(0);
        TreeTableView<Pair<String, MetaExpression>> nodeTableView = getTreeTableView(node);

        expandItem(treeOccurrence.getItem());
        nodeTableView.getSelectionModel().select(treeOccurrence.getItem());

        int row = nodeTableView.getRow(treeOccurrence.getItem());

        nodeTableView.scrollTo(row);
    }

    private void expandItem(final TreeItem<Pair<String, MetaExpression>> item) {
        item.setExpanded(true);
        TreeItem<Pair<String, MetaExpression>> parent = item.getParent();
        if (parent != null) {
            expandItem(parent);
        }
    }

    private void searchText() {
        Matcher matcher = regexPattern.matcher(text.getText());

        // Find all occurrences
        occurrences.clear();
        while (matcher.find()) {
            occurrences.add(new SearchTextOccurrence(matcher.start(), matcher.group()));
        }
    }

    private void searchTreeIterate(TreeItem<Pair<String, MetaExpression>> parent) {

        parent.getChildren().forEach(item -> {

            String key = item.getValue().getKey();
            if (regexPattern.matcher(key).find()) {
                occurrences.add(new SearchTreeOccurrence(item));
            } else if (item.isLeaf()) {
                MetaExpression value = item.getValue().getValue();

                if (regexPattern.matcher(value.getStringValue()).find()) {
                    occurrences.add(new SearchTreeOccurrence(item));
                }
            }
            if (!item.isLeaf() && !item.isExpanded()) {
                item.setExpanded(true);
                searchTreeIterate(item);
                item.setExpanded(false);
            } else {
                searchTreeIterate(item);
            }
        });
    }
}
