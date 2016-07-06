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

import nl.xillio.migrationtool.elasticconsole.ESConsoleClient.LogType;
import nl.xillio.migrationtool.elasticconsole.ESConsoleClient.SearchFilter;
import nl.xillio.migrationtool.elasticconsole.filters.KeywordFilter;
import nl.xillio.migrationtool.elasticconsole.filters.LogTypeFilter;
import nl.xillio.migrationtool.elasticconsole.filters.RegexFilter;
import nl.xillio.migrationtool.virtuallist.DataProvider;
import nl.xillio.migrationtool.virtuallist.Filter;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A {@link DataProvider} implementation that wraps an ElasticSearch database
 * <p>
 * @author Ernst van Rheenen
 */
public class ESDataProvider implements DataProvider<LogEntry> {
    // Date formatting for our console
    private static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // ID of the log
    private String logId;                         // The ID of the logfile to be queried
    // Filters
    private SearchFilter filter;  // The filter to be applied on queries
    private Map<LogType, Boolean> logTypeFilters;  // List of logtypes that are filtered
    private String pattern = "";
    private boolean useRegex;

    /**
     * Create a new ESDataProvider that connects to the log file with provided logId.
     *
     * @param logId The log file to connect to
     */
    public ESDataProvider(String logId) {
        this.logId = logId;
    }

    @Override
    public int getSize() {
        return ESConsoleClient.getInstance().countFilteredEntries(logId, filter);
    }

    @Override
    public Counter getFilteredSizes() {
        Counter sizes = new Counter<>();
        for (LogType logType : logTypeFilters.keySet()) {
            int count = 0;


            Map<LogType, Boolean> subFilter = new HashMap<>();
            for (LogType type : LogType.values()) {
                subFilter.put(type, type == logType);
            }

            SearchFilter subSearchFilter = ESConsoleClient.getInstance().createSearchFilter(pattern, useRegex, subFilter);

            //if (logTypeFilters.get(logType) == true) {
            count = ESConsoleClient.getInstance().countFilteredEntries(logId, subSearchFilter);
            //}
            sizes.put(logType, count);
        }

        return sizes;
    }

    @Override
    public List<LogEntry> getBatch(int start, int end) {
        List<LogEntry> logEntries = new LinkedList<>();

        // Fetch items from ES
        List<Map<String, Object>> entries = ESConsoleClient.getInstance().getFilteredEntries(logId, start, end, filter);
        for (Map<String, Object> entry : entries) {

            // Get all properties
            String time = DATEFORMAT.format(new Date((long) entry.get("timestamp")));
            LogType type = LogType.valueOf(entry.get("type").toString().toUpperCase());
            String message = "" + entry.get("message");

            // Add log entry
            logEntries.add(new LogEntry(time, type, message, false));
        }

        return logEntries;
    }

    @Override
    public void setSearchFilters(List<Filter> filters) {
        // Note: The implementation of ESConsoleClient.SearchFilter is a bit awkward, we just make do here
        logTypeFilters = new HashMap<>();
        pattern = "";
        useRegex = false;

        // Set all output to false by default
        for (LogType type : LogType.values()) {
            logTypeFilters.put(type, false);
        }

        // Loop over filters
        for (Filter filter : filters) {
            if (filter instanceof KeywordFilter) {
                pattern = ((KeywordFilter) filter).getFilter();
                useRegex = false;
            } else if (filter instanceof RegexFilter) {
                pattern = ((RegexFilter) filter).getFilter();
                useRegex = true;
            } else if (filter instanceof LogTypeFilter) {
                logTypeFilters.put(((LogTypeFilter) filter).getFilter(), true);

            }
        }

        // Set the final filter
        filter = ESConsoleClient.getInstance().createSearchFilter(pattern, useRegex, logTypeFilters);
    }
}
