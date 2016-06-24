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

import me.biesaart.utils.Log;
import nl.xillio.events.Event;
import nl.xillio.events.EventHost;
import nl.xillio.migrationtool.RobotAppender;
import nl.xillio.util.XillioHomeFolder;
import nl.xillio.xill.api.LogUtil;
import nl.xillio.xill.api.components.RobotID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ESConsoleClient implements AutoCloseable {

    private static final Logger LOGGER = Log.get();

    // A mapping from robot id to an index.
    private static final Map<String, String> indices = new HashMap<>();
    private static final String botIndexPrefix = "bot_";
    private static int robotIndexCount = 0;

    @Override
    public void close() {
        node.close();
    }

    public class SearchFilter {
        private String text = ""; // needle or pattern
        private boolean regExp;
        private Map<LogType, Boolean> types;

        private String getWildcardNeedle() {
            return String.format("*%1$s*", text);
        }

        void setCountRequestFilter(CountRequestBuilder request) {
            if (!this.text.isEmpty()) {
                if (this.regExp) {
                    request.setQuery(QueryBuilders.regexpQuery("message", this.text));
                } else {
                    request.setQuery(QueryBuilders.wildcardQuery("message", this.getWildcardNeedle()));
                }
            }

            ArrayList<String> typeList = new ArrayList<String>();
            if (types.get(LogType.DEBUG)) {
                typeList.add("debug");
            }
            if (types.get(LogType.INFO)) {
                typeList.add("info");
            }
            if (types.get(LogType.WARN)) {
                typeList.add("warn");
            }
            if (types.get(LogType.ERROR)) {
                typeList.add("error");
            }
            if (types.get(LogType.FATAL)) {
                typeList.add("fatal");
            }
            request.setTypes(typeList.toArray(new String[typeList.size()]));
        }

        public void setSearchRequestFilter(SearchRequestBuilder request) {
            if (!this.text.isEmpty()) {
                if (this.regExp) {
                    request.setQuery(QueryBuilders.regexpQuery("message", this.text));
                } else {
                    request.setQuery(QueryBuilders.wildcardQuery("message", this.getWildcardNeedle()));
                }
            }

            ArrayList<String> typeList = new ArrayList<String>();
            if (types.get(LogType.DEBUG)) {
                typeList.add("debug");
            }
            if (types.get(LogType.INFO)) {
                typeList.add("info");
            }
            if (types.get(LogType.WARN)) {
                typeList.add("warn");
            }
            if (types.get(LogType.ERROR)) {
                typeList.add("error");
            }
            if (types.get(LogType.FATAL)) {
                typeList.add("fatal");
            }
            request.setTypes(typeList.toArray(new String[typeList.size()]));

        }
    }

    public SearchFilter createSearchFilter(String text, boolean regExp, Map<LogType, Boolean> types) {
        SearchFilter filter = new SearchFilter();
        filter.text = text;
        filter.regExp = regExp;
        filter.types = types;
        return filter;
    }

    /**
     * The different logging message types.
     */
    public enum LogType {
        DEBUG, INFO, WARN, ERROR, FATAL
    }

    private static final ESConsoleClient instance = new ESConsoleClient();
    private static final Map<String, EventHost<RobotLogMessage>> eventInstances = new HashMap<>();

    // A list of characters that are illegal in the index and type
    private static final Pattern illegalChars = Pattern.compile("[\\\\/*?\"<>| ,:#]");

    private final Node node;
    private int order = 0;

    private ESConsoleClient() {
        // Create the settings and node
        File ESConsole = new File(XillioHomeFolder.forXillIDE(), "ESConsole");

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", "console")
                // Paths
                .put("path.data", new File(ESConsole, "data").getAbsolutePath())
                .put("path.logs", new File(ESConsole, "logs").getAbsolutePath())
                .put("path.work", new File(ESConsole, "work").getAbsolutePath())
                // Make the node unreachable from the outside
                .put("discovery.zen.ping.multicast.enabled", false)
                .put("node.local", true)
                .put("http.enabled", false)
                .build();
        node = NodeBuilder.nodeBuilder().settings(settings).node();

        // Add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(node::close));

        // Add the appender
        org.apache.logging.log4j.core.Logger robotLogger = (org.apache.logging.log4j.core.Logger) LogManager.getLogger("robot");
        Appender appender = RobotAppender.createAppender("ctLogger", null, null);
        robotLogger.addAppender(appender);
        appender.start();

        // Subscribe to the hook
        RobotAppender.getMessagelogged().addListener(event -> {
            String name = event.getLoggerName();

            if (name.startsWith(LogUtil.ROBOT_LOGGER_PREFIX)) {
                String robotId = name.substring(LogUtil.ROBOT_LOGGER_PREFIX.length());
                ESConsoleClient.getInstance().log(
                        robotId,
                        event.getLevel().toString(),
                        event.getTimeMillis(),
                        order++,
                        event.getMessage().getFormattedMessage());
            }
        });

    }

    /**
     * Logs a message.
     *
     * @param robotId   The robot ID of the robot that called the log.
     * @param type      The log type.
     * @param timestamp The timestamp of the log message.
     * @param order     The order of messages (for messages logged in the same timestamp).
     * @param message   The message to log.
     */
    public void log(final String robotId, String type, final long timestamp, final int order, final String message) {

        // Normalize index and type
        EventHost<RobotLogMessage> eventHost = eventInstances.get(robotId);
        String index = getIndex(robotId);
        type = normalize(type);

        // Create the index
        createIndex(index);

        // Create the map
        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", timestamp);
        data.put("order", order);
        data.put("message", message);

        // Index the log message in robotId/type/
        IndexRequestBuilder request = getClient().prepareIndex(index, type).setSource(data);
        request.execute().actionGet();

        // Fire event if there is one
        if (eventHost != null) {
            eventHost.invoke(new RobotLogMessage(type, message));
        }
    }

    /**
     * Create the index (with default log mapping) if it does not exist yet.
     *
     * @param index The index to create.
     */
    private synchronized void createIndex(final String index) {
        // Check if the index exists
        if (getClient().admin().indices().prepareExists(index).execute().actionGet().isExists()) {
            return;
        }

        // Create the index creation request with all mapping
        CreateIndexRequest request = new CreateIndexRequest(index);
        for (LogType type : LogType.values()) {
            request.mapping(type.toString().toLowerCase(), getLogMapping(type));
        }

        // Create the index
        getClient().admin().indices().create(request).actionGet();
    }

    /**
     * Get the default mapping for log entries in elasticsearch.
     *
     * @param type The type to create the mapping source for.
     * @return An XContentBuilder that contains the source for the mapping.
     */
    private static XContentBuilder getLogMapping(final LogType type) {
        try {
            // Create the content builder
            XContentBuilder source = XContentFactory.jsonBuilder();

            // Create the source
            source.startObject().startObject(type.toString().toLowerCase()).startObject("properties");
            // Set the field types
            source.startObject("timestamp").field("type", "long").field("store", true).endObject();
            source.startObject("order").field("type", "integer").field("store", true).endObject();
            source.startObject("message").field("type", "string").field("store", true).endObject();
            // End
            source.endObject().endObject().endObject();

            return source;
        } catch (IOException e) {
            LOGGER.error("Content builder creation failed", e);
            return null;
        }
    }

    public int countFilteredEntries(final String robotId, final SearchFilter searchFilter) {
        // Normalize index
        String index = getIndex(robotId);

        CountRequestBuilder request = getClient().prepareCount(index);

        if (searchFilter != null) {
            searchFilter.setCountRequestFilter(request);
        }
        if (!getClient().admin().indices().prepareExists(index).execute().actionGet().isExists()) {
            return 0; //if no indices exist return 0;
        }

        CountResponse response = null;
        try {
            // Refresh the index to make sure everything is properly indexed etc
            getClient().admin().indices().prepareRefresh(index).execute().actionGet();
            // Get the response
            response = request.execute().actionGet();
        } catch (SearchPhaseExecutionException | IndexMissingException e) {
            LOGGER.error(e.getMessage(), e);
        }

        // Return the count
        return (int) response.getCount();
    }

    public ArrayList<Map<String, Object>> getFilteredEntries(final String robotId, final int from, final int to, final SearchFilter searchFilter) {
        // Normalize index
        String index = getIndex(robotId);

        SearchRequestBuilder request = getClient().prepareSearch(index).setQuery(QueryBuilders.matchAllQuery());

        if (searchFilter != null) {
            searchFilter.setSearchRequestFilter(request);
        }

        request.addSort("timestamp", SortOrder.ASC).addSort("order", SortOrder.ASC)
                .setFrom(from)
                .setSize(to - from + 1);

        return this.getEntries(index, request);
    }

    private ArrayList<Map<String, Object>> getEntries(final String normalizedId, final SearchRequestBuilder request) {
        if (!getClient().admin().indices().prepareExists(normalizedId).execute().actionGet().isExists()) {
            return new ArrayList<>(0); //if no indices exist return empty list
        }
        SearchResponse response = null;
        try {
            // Refresh the index to make sure everything is properly indexed etc
            getClient().admin().indices().prepareRefresh(normalizedId).execute().actionGet();
            // Get the response
            response = request.execute().actionGet();
        } catch (SearchPhaseExecutionException | IndexMissingException e) {
            LOGGER.error("Failed to get entries: " + e.getMessage(), e);
        }

        // Return the hits as a list of maps
        return hitsToList(response.getHits());
    }

    private static ArrayList<Map<String, Object>> hitsToList(final SearchHits hits) {
        // Get the search hits, create a result list
        SearchHit[] searchHits = hits.getHits();
        ArrayList<Map<String, Object>> results = new ArrayList<>(searchHits.length);

        // Add all hits to the list
        for (int i = 0; i < searchHits.length; i++) {
            results.add(searchHits[i].getSource());
            results.get(i).put("type", searchHits[i].type());
        }

        return results;
    }

    /**
     * Clear the log for a robot.
     *
     * @param robotId The robot ID of the robot to clear the log for.
     */
    public void clearLog(final String robotId) {
        // Get the index for the robot.
        String index = getIndex(robotId);
        indices.remove(index);

        // Delete all documents in the robotId index and remove it from the index mapping.
        try {
            getClient().admin().indices().delete(new DeleteIndexRequest(index));
        } catch (IndexMissingException e) {
            // If the index does not exist, do nothing.
            LOGGER.error("The index does not exist.", e);
        }
    }

    private static String getIndex(final String id) {
        // Check if the id is already mapped. If not, generate an id.
        if (!indices.containsKey(id)) {
            String index = botIndexPrefix + robotIndexCount++;
            indices.put(id, index);
        }
        return indices.get(id);
    }

    private static String normalize(final String text) {
        // Replace illegal characters by an underscore, and lowercase.
        return illegalChars.matcher(text).replaceAll("_").toLowerCase();
    }

    /**
     * @return the {@link Client}
     */
    public Client getClient() {
        return node.client();
    }

    /**
     * Get the elasticsearch console client.
     *
     * @return The running ESConsoleClient instance.
     */
    public static ESConsoleClient getInstance() {
        return instance;
    }

    /**
     * Gets the log event corresponding to the {@link RobotID}
     *
     * @param robotId the id of the robot to get the log event for
     * @return The event
     */
    public static Event<RobotLogMessage> getLogEvent(final RobotID robotId) {

        synchronized (eventInstances) {
            EventHost<RobotLogMessage> host = eventInstances.get(robotId.toString());

            if (host == null) {
                host = new EventHost<>();
                eventInstances.put(robotId.toString(), host);
            }

            return host.getEvent();
        }
    }
}
