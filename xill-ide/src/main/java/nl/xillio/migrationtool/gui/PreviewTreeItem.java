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

import javafx.scene.control.TreeItem;
import javafx.util.Pair;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides preview of {@link MetaExpression}s in {@link PreviewPane}.
 *
 * @author Thomas Biesaart
 * @author Folkert van Verseveld
 */
public class PreviewTreeItem extends TreeItem<Pair<String, MetaExpression>> {
    private boolean hasExpanded = false;

    PreviewTreeItem(String name, MetaExpression value) {
        super(new Pair<>(name, value));
        // Lazily expand item
        expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                onExpand();
            }
        });
    }

    /**
     * Check whether it has no children.
     *
     * @return {@code true} if and only if type is {@link ExpressionDataType#ATOMIC}.
     */
    @Override
    public boolean isLeaf() {
        if (getValue().getValue().isClosed()) {
            return true; // It's closed so we treat it as a leaf
        } else {
            return getValue().getValue().getType() == ExpressionDataType.ATOMIC;
        }
    }

    /**
     * Lazily expand item and make sure we construct children only once.
     */
    private void onExpand() {
        if (hasExpanded) {
            return;
        }
        hasExpanded = true;

        MetaExpression expression = getValue().getValue();

        switch (expression.getType()) {
            case LIST:
                buildListChildren();
                break;
            case OBJECT:
                buildObjectChildren();
                break;
            case ATOMIC:
                break;
            default:
                // Should never happen
                throw new NotImplementedException("Invalid type: " + expression.getType());
        }
    }

    /**
     * Construct numbered mapping of list children.
     */
    private void buildListChildren() {
        List<PreviewTreeItem> children = new ArrayList<>();
        List<MetaExpression> expressions = getValue().getValue().getValue();
        int counter = 0;
        for (MetaExpression expression : expressions) {
            children.add(new PreviewTreeItem(Integer.toString(counter++), expression));
        }
        getChildren().setAll(children);
    }

    /**
     * Construct mapping of object children.
     */
    private void buildObjectChildren() {
        List<PreviewTreeItem> children = new ArrayList<>();
        Map<String, MetaExpression> expressions = getValue().getValue().getValue();
        children.addAll(
                expressions.entrySet()
                        .stream()
                        .map(pair -> new PreviewTreeItem(pair.getKey(), pair.getValue()))
                        .collect(Collectors.toList())
        );
        getChildren().setAll(children);
    }
}
