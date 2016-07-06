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
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

public class ESConsoleClient implements AutoCloseable {

    private static final Logger LOGGER = Log.get();

    // A mapping from robot id to an index.
    private static final Map<String, String> indices = new HashMap<>();
    private static final ESConsoleClient INSTANCE = new ESConsoleClient();
    private static final Map<String, EventHost<RobotLogMessage>> EVENT_INSTANCES = new HashMap<>();
    // A list of characters that are illegal in the index and type
    private static final Pattern ILLEGAL_CHARS = Pattern.compile("[\\\\/*?\"<>| ,:#]");
    private final Node node;
    private int order;

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
        Runtime.getRuntime().addShutdownHook(new Thread(node::close, "ES Teardown"));

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
                INSTANCE.log(
                        robotId,
                        event.getLevel().toString(),
                        event.getTimeMillis(),
                        order,
                        event.getMessage().getFormattedMessage());
                order++;
            }
        });

    }

    /**
     * Gets the default mapping for log entries in elasticsearch.
     *
     * @param type the type to create the mapping source for
     * @return an builder that contains the source for the mapping
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

    private static List<Map<String, Object>> hitsToList(final SearchHits hits) {
        // Get the search hits, create a result list
        SearchHit[] searchHits = hits.getHits();
        List<Map<String, Object>> results = new ArrayList<>(searchHits.length);

        // Add all hits to the list
        for (int i = 0; i < searchHits.length; i++) {
            results.add(searchHits[i].getSource());
            results.get(i).put("type", searchHits[i].type());
        }

        return results;
    }

    private static String getIndex(final String id) {
        // Check if the id is already mapped. If not, generate an id.
        if (!indices.containsKey(id)) {
            indices.put(id, toMD5(id));
        }
        return indices.get(id);
    }

    /**
     * Replaces illegal characters by an underscore, and lowercase.
     *
     * @param text the text to normalize
     * @return the normalized result
     */
    private static String normalize(final String text) {
        return ILLEGAL_CHARS.matcher(text).replaceAll("_").toLowerCase();
    }

    /**
     * Gets the elasticsearch console client.
     *
     * @return the running ESConsoleClient instance
     */
    public static ESConsoleClient getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the log event corresponding to the RobotID.
     *
     * @param robotId the id of the robot to get the log event for
     * @return the event
     */
    public static Event<RobotLogMessage> getLogEvent(final RobotID robotId) {

        synchronized (EVENT_INSTANCES) {
            EventHost<RobotLogMessage> host = EVENT_INSTANCES.get(robotId.toString());

            if (host == null) {
                host = new EventHost<>();
                EVENT_INSTANCES.put(robotId.toString(), host);
            }

            return host.getEvent();
        }
    }

    @Override
    public void close() {
        node.close();
    }

    public SearchFilter createSearchFilter(String text, boolean regExp, Map<LogType, Boolean> types) {
        SearchFilter filter = new SearchFilter();
        filter.text = text;
        filter.regExp = regExp;
        filter.types = types;
        return filter;
    }

    /**
     * Logs a message.
     *
     * @param robotId   the ID of the robot that called the log
     * @param type      the log type
     * @param timestamp the timestamp of the log message
     * @param order     the order of messages (for messages logged in the same timestamp)
     * @param message   the message to log
     */
    public void log(final String robotId, String type, final long timestamp, final int order, final String message) {

        // Normalize index and type
        EventHost<RobotLogMessage> eventHost = EVENT_INSTANCES.get(robotId);
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
            //Throttle events, to prevent overloading the observer
            eventHost.invoke(new RobotLogMessage(type, message));
        }
    }

    /**
     * Creates the index (with default log mapping) if it does not exist yet.
     *
     * @param index the index to create
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

    public int countFilteredEntries(final String robotId, final SearchFilter searchFilter) {
        // Fix for issue: If no types are selected, all entries are shown just as if all types were selected
        // If all types are deselected: return empty result
        if(searchFilter!=null && searchFilter.getActiveTypeCount() == 0) {
            return 0;
        }

        // Normalize index
        String index = getIndex(robotId);

        Client client = getClient();

        CountRequestBuilder request = client.prepareCount(index);

        if (searchFilter != null) {
            searchFilter.setCountRequestFilter(request);
        }

        CountResponse response = null;

        // Get the response
        boolean validResponse = false;
        do { // workaround ES index related bug...
            try {
                createIndex(index);

                // Refresh the index to make sure everything is properly indexed etc
                client.admin().indices().prepareRefresh(index).execute().actionGet();
                response = request.execute().actionGet();
                validResponse = true;
            } catch (IndexMissingException e) {
                //safely ignore...
            }
        } while (!validResponse);

        // Return the count
        return (int) response.getCount();
    }

    public List<Map<String, Object>> getFilteredEntries(final String robotId, final int from, final int to, final SearchFilter searchFilter) {
        // Fix for issue: If no types are selected, all entries are shown just as if all types were selected
        // If all types are deselected: return empty result
        if(searchFilter!=null && searchFilter.getActiveTypeCount() == 0) {
            return new ArrayList<>(0);
        }

        // Normalize index
        String index = getIndex(robotId);

        SearchRequestBuilder request = getClient().prepareSearch(index).setQuery(QueryBuilders.matchAllQuery());

        if (searchFilter != null) {
            searchFilter.setSearchRequestFilter(request);
        }

        request.addSort("timestamp", SortOrder.ASC).addSort("order", SortOrder.ASC)
                .setFrom(from)
                .setSize(to - from + 1);

        return getEntries(index, request);
    }

    private List<Map<String, Object>> getEntries(final String normalizedId, final SearchRequestBuilder request) {
        if (!getClient().admin().indices().prepareExists(normalizedId).execute().actionGet().isExists()) {
            return new ArrayList<>(0); //if no indices exist return empty list
        }
        SearchResponse response;
        try {
            // Refresh the index to make sure everything is properly indexed etc
            getClient().admin().indices().prepareRefresh(normalizedId).execute().actionGet();
            // Get the response
            response = request.execute().actionGet();
        } catch (SearchPhaseExecutionException | IndexMissingException e) {
            LOGGER.error("Failed to get entries: " + e.getMessage(), e);
            return new ArrayList<>(0);
        }

        // Return the hits as a list of maps
        return hitsToList(response.getHits());
    }

    /**
     * Clears the log for a robot.
     *
     * @param robotId the robot ID of the robot to clear the log for
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

    /**
     * @return the client
     */
    public Client getClient() {
        return node.client();
    }

    /**
     * The different logging message types.
     */
    public enum LogType {
        DEBUG, INFO, WARN, ERROR, FATAL
    }

    private static String toMD5(String text) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(text.getBytes(), 0, text.length());
            return new BigInteger(1, m.digest()).toString(16).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public class SearchFilter {
        private String text = ""; // needle or pattern
        private boolean regExp;
        private Map<LogType, Boolean> types;

        private String getWildcardNeedle() {
            return String.format("*%1$s*", text);
        }

        void setCountRequestFilter(CountRequestBuilder request) {
            if (!text.isEmpty()) {
                if (regExp) {
                    request.setQuery(QueryBuilders.regexpQuery("message", text));
                } else {
                    request.setQuery(QueryBuilders.wildcardQuery("message", getWildcardNeedle()));
                }
            }

            List<String> typeList = new ArrayList<>();
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
            if (!text.isEmpty()) {
                if (regExp) {
                    request.setQuery(QueryBuilders.regexpQuery("message", text));
                } else {
                    request.setQuery(QueryBuilders.wildcardQuery("message", getWildcardNeedle()));
                }
            }

            List<String> typeList = new ArrayList<>();
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

        public int getActiveTypeCount() {
            int count = 0;
            for(Map.Entry<LogType, Boolean> entry: types.entrySet()) {
                if(entry.getValue()) {
                    count ++;
                }
            }
            return count;
        }

    }
}
