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
package nl.xillio.migrationtool.elasticconsole;

import javafx.beans.property.SimpleStringProperty;
import nl.xillio.migrationtool.elasticconsole.ESConsoleClient.LogType;

public class LogEntry {
    // These are used by the cell value factories from the console tableview
    private final SimpleStringProperty showTime, showType, line;

    private final boolean newLine;
    private final LogType type;
    private boolean selected = false;

    /**
     * Create a new log entry.
     *
     * @param time    The time of the entry.
     * @param type    The type of the entry (info, debug...)
     * @param line    The message of the entry (a single line).
     * @param newLine Whether this entry is a new line.
     */
    public LogEntry(final String time, final LogType type, final String line, final boolean newLine) {
        // Visible attributes, empty for new lines
        showTime = new SimpleStringProperty(newLine ? "" : time);
        showType = new SimpleStringProperty(newLine ? "" : type.toString());

        this.type = type;
        this.line = new SimpleStringProperty(line);
        this.newLine = newLine;
    }

    /**
     * @return The type of this entry.
     */
    public LogType getType() {
        return type;
    }

    /**
     * @return The message (line) of this entry.
     */
    public String getLine() {
        return line.get();
    }

    /**
     * @return The time of this entry, or an empty string if this is a new line.
     */
    public String getShowTime() {
        return showTime.get();
    }

    /**
     * @return The type of this entry, or an empty string if this is a new line.
     */
    public String getShowType() {
        return showType.get();
    }

    /**
     * True if this line is a new line, false if this is the first line.
     *
     * @return Whether this line is a new line.
     */
    public boolean isNewLine() {
        return newLine;
    }

    /**
     * @param selected if this line contains the expression that is being searched for (search purposes)
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * @return true if this line is selected (for a search purposes)
     */
    public boolean getSelected() {
        return selected;
    }

}
