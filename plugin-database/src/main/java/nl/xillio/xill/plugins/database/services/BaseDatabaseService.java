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
package nl.xillio.xill.plugins.database.services;

import com.google.common.collect.Iterators;
import me.biesaart.utils.Log;
import nl.xillio.xill.api.components.EventEx;
import nl.xillio.xill.plugins.database.util.StatementIterator;
import nl.xillio.xill.plugins.database.util.Tuple;
import nl.xillio.xill.plugins.database.util.TypeConverter;
import nl.xillio.xill.plugins.database.util.TypeConverter.ConversionException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The base service for any databaseService.
 */
public abstract class BaseDatabaseService implements DatabaseService {

    private static final Pattern PARAMETER_PATTERN = Pattern.compile("(?<!\\\\):([a-zA-Z_\\d]+)");
    private static final Logger LOGGER = Log.get();

    /**
     * Cache for delimiters, prevents from constantly getting the metadata
     */
    private LinkedHashMap<Connection, String> delimiter = new LinkedHashMap<>();

    /**
     * Create a JDBC {@link Connection} to the database at the given URL
     * if no URL is given then return the last connection that was made in the robot.
     *
     * @param url URL to connect to
     * @return A connection ready to execute queries on
     * @throws SQLException
     */
    protected Connection connect(final String url) throws SQLException {
        return DriverManager.getConnection(url);
    }

    /**
     * Create a JDBC {@link Connection} to the database at the given URL, using the given properties. Especially useful for connecting to an Oracle database.
     *
     * @param url        URL to connect to
     * @param properties Separate properties object
     * @return A connection ready to execute queries on
     */
    protected Connection connect(final String url, final Properties properties) throws SQLException {
        return DriverManager.getConnection(url, properties);
    }

    @Override
    public void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e1) {
            LOGGER.error("Failed to close a connection.", e1);
        } catch (NullPointerException e2) {
            LOGGER.warn("No connection found when trying to close a connection", e2);
        }
    }

    @Override
    public Object query(final Connection connection, final String query, final int timeout) throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.setQueryTimeout(timeout);
        stmt.execute(query);

        return createQueryResult(stmt);
    }

    private void interruptibleExec(final PreparedStatement stmt, final EventEx<Object> interruptEvent) throws SQLException {
        if (interruptEvent == null) {
            stmt.execute();
        } else {
            Consumer<Object> stopStmt = o -> {
                try {
                    stmt.cancel();
                } catch (SQLException ex) {
                    LOGGER.warn("Cannot cancel running SQL statement!", ex);
                }
            };
            interruptEvent.addListener(stopStmt);
            try {
                stmt.execute();
            } finally {
                interruptEvent.removeListener(stopStmt);
            }
        }
    }

    @Override
    public Object preparedQuery(final Connection connection, final String query, final List<LinkedHashMap<String, Object>> parameters, final int timeout, final EventEx<Object> interruptEvent) throws SQLException {
        PreparedStatement stmt = parseNamedParameters(connection, query);
        stmt.setQueryTimeout(timeout);

        if (parameters == null || parameters.isEmpty()) {
            if (!extractParameterNames(query).isEmpty()) {
                throw new IllegalArgumentException("Parameters is empty for parametrised query.");
            }
            interruptibleExec(stmt, interruptEvent);
        } else if (parameters.size() == 1) {
            LinkedHashMap<String, Object> parameter = parameters.get(0);
            fillStatement(parameter, stmt, 1);
            interruptibleExec(stmt, interruptEvent);
        } else {
            // convert int[] to Integer[] to be able to create an iterator.
            Integer[] updateCounts = ArrayUtils.toObject(executeBatch(stmt, extractParameterNames(query), parameters));

            return (Arrays.asList(updateCounts)).iterator();
        }

        return createQueryResult(stmt);
    }

    /**
     * Create an Object representing the result of a query.
     *
     * @param stmt Statement on which a query has been executed
     * @return An Integer if the query represents one insert or update, an iterator otherwise
     * @throws SQLException
     */
    private Object createQueryResult(Statement stmt) throws SQLException {
        // If the first result is the only result and an update count simply return that count, otherwise create an iterator over the statement
        int firstCount = stmt.getUpdateCount();
        if (firstCount != -1) {
            boolean more = stmt.getMoreResults();
            int secondCount = stmt.getUpdateCount();
            if (!more && secondCount == -1) {
                return firstCount;
            } else {
                // Append the already retrieved count to a new statement iterator
                return Iterators.concat(Iterators.forArray(firstCount), new StatementIterator(stmt));
            }
        }
        return new StatementIterator(stmt);
    }

    /**
     * Parse a {@link PreparedStatement} from a query with named parameters
     *
     * @param connection
     * @param query      The query to parse
     * @return An unused {@link PreparedStatement}.
     * @throws SQLException
     */
    PreparedStatement parseNamedParameters(final Connection connection, final String query) throws SQLException {
        Matcher m = PARAMETER_PATTERN.matcher(query);

        String preparedQuery = m.replaceAll("?");
        return connection.prepareStatement(preparedQuery);
    }

    /**
     * @param query
     * @return The names of the parameters in the given query in order of appearance
     */
    List<String> extractParameterNames(final String query) {
        Matcher m = PARAMETER_PATTERN.matcher(query);
        List<String> indexedParameters = new ArrayList<>();
        while (m.find()) {
            String paramName = m.group(1);
            indexedParameters.add(paramName);
        }
        return indexedParameters;
    }

    /**
     * @param stmt              the prepared statement
     * @param indexedParameters the list with the parameters
     * @param parameters        the list of maps
     * @return an array of update counts
     * @throws SQLException
     */
    int[] executeBatch(final PreparedStatement stmt, final List<String> indexedParameters, final List<LinkedHashMap<String, Object>> parameters) throws SQLException {
        for (LinkedHashMap<String, Object> parameter : parameters) {
            for (int i = 0; i < indexedParameters.size(); i++) {
                String indexedParameter = indexedParameters.get(i);
                // returns null on null value and when key is not contained in map
                if (!parameter.containsKey(indexedParameter)) {
                    throw new IllegalArgumentException("The Parameters argument should contain: \"" + indexedParameter + "\"");
                }

                Object value = parameter.get(indexedParameter);
                stmt.setObject(i + 1, value);
            }
            stmt.addBatch();
        }
        return stmt.executeBatch();
    }

    /**
     * Create a connection string for a JDBC connection
     *
     * @param database Database name
     * @param user     Username
     * @param pass     Password
     * @param options  JDBC specific options
     * @return Connection String
     * @throws SQLException When a database error occurs
     */
    protected abstract String createConnectionURL(String database, String user, String pass, Tuple<String, String>... options) throws SQLException;

    /**
     * Loads the JDBC driver needed for this service to function. Should use {@link Class#forName(String)} in most cases.
     *
     * @throws ClassNotFoundException
     */
    public abstract void loadDriver() throws ClassNotFoundException;

    /**
     * @param type     JDBC name of the database
     * @param database host, port and database name in the correct format for the JDBC driver
     * @param user     can be null
     * @param pass     can be null
     * @param options  Driver specific options
     * @return A URL to connect to the database
     */
    @SuppressWarnings("squid:S2068")
    // Credentials should not be hard-coded.
    String createJDBCURL(final String type, final String database, final String user, final String pass, String optionsMarker, String optionsSeparator, final Tuple<String, String>... options) {
        String url = String.format("jdbc:%s://%s", type, database);

        // question mark for url parameters
        url = url + optionsMarker;

        // append user
        if (user != null) {
            url = String.format("%suser=%s%s", url, user, optionsSeparator);
        }
        // append password
        if (pass != null) {
            url = String.format("%spassword=%s%s", url, pass, optionsSeparator);
        }
        // append other options
        for (Tuple<String, String> option : options) {
            url = String.format("%s%s=%s%s", url, option.getKey(), option.getValue(), optionsSeparator);
        }
        return url;
    }

    @Override
    public LinkedHashMap<String, Object> getObject(final Connection connection, final String table, final Map<String, Object> constraints)
            throws SQLException, ConversionException {
        // prepare statement
        final LinkedHashMap<String, Object> notNullConstraints = new LinkedHashMap<>();
        constraints.forEach((k, v) -> {
            if (v != null) {
                notNullConstraints.put(k, v);
            }
        });

        String query = createSelectQuery(connection, table, new ArrayList<String>(notNullConstraints.keySet()));
        PreparedStatement statement = connection.prepareStatement(query);

        // Fill out values
        fillStatement(notNullConstraints, statement, 1);

        // perform query

        ResultSet result = statement.executeQuery();
        ResultSetMetaData metadata = result.getMetaData();

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        if (result.next()) {
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                String here = metadata.getColumnName(i);
                Object value = result.getObject(i);
                map.put(here, TypeConverter.convertJDBCType(value));
            }
            statement.close();
            return map;
        }
        statement.close();
        return null;
    }

    void setValue(final PreparedStatement statement, final String key, final Object value, final int i) throws SQLException {
        try {
            // All supported databases allow setObject(i, null), so no setNull needed
            statement.setObject(i, value);
        } catch (Exception e) {
            throw new SQLException("Failed to set value '" + value + "' for column '" + key + "'.", e);
        }
    }

    String createSelectQuery(final Connection connection, final String table, final List<String> keys) throws SQLException {

        // creates WHERE conditions SQL string
        String constraintsSql;
        if (!keys.isEmpty()) {
            constraintsSql = createQueryPart(connection, keys, " AND ");
        } else {
            constraintsSql = "1";
        }

        // creates entire SQL query according to DB type
        return createSelectQuery(escapeTableName(table), constraintsSql);
    }

    /**
     * Create a select query for getting one row from the database
     *
     * @param constraintsSql Constrainsts for selecting (containing AND, OR, etc.)
     * @param table          The table to select from
     * @return A SQL query that selects one row using the given constraints
     */
    String createSelectQuery(final String table, final String constraintsSql) {
        return String.format("SELECT * FROM %1$s WHERE %2$s LIMIT 1", table, constraintsSql);
    }

    /**
     * Store an object in the database.
     * Update the database when a row already exists and allowUpdate is true.
     * Do nothing when a row already exists and allowUpdate is false
     * Insert into the database when a row does not exist
     *
     * @param connection  The JDBC connection.
     * @param table       The name of the table.
     * @param newObject   The object we want to store.
     * @param keys        The keys we hand the object.
     * @param allowUpdate A boolean whether we allow an to update an entry or not.
     * @throws SQLException
     */
    // We need a map with guaranteed insertion order, there is no interface available, only the LinkedHashMap implementation is available
    @Override
    public void storeObject(final Connection connection, final String table, final LinkedHashMap<String, Object> newObject, final List<String> keys, final boolean allowUpdate)
            throws SQLException {

        boolean exists;
        try {
            // check if an entry with the given keys exists
            exists = checkIfExists(connection, table, newObject, keys);
        } catch (ConversionException e) {
            throw new SQLException(e);
        }
        if (!keys.isEmpty() && exists) {
            if (allowUpdate) {
                // an entry exists and we are allowed to update it
                updateObject(connection, table, newObject, keys);
            } else {
                // an entry exists but we are not allowed to update it so we do not do anything
                return;
            }
        } else {
            // no existing entry found so we insert the object.
            insertObject(connection, table, newObject);
        }
    }

    /**
     * Return true if the provided table has a row with the provided properties. In all other cases: return false.
     *
     * @param connection
     * @param table
     * @param newObject
     * @param keys
     * @return True when the row exists, false otherwise
     * @throws SQLException
     * @throws ConversionException
     */
    @SuppressWarnings("squid:S1166")
    // The illegal argument exception is expected and should always result in "false" being returned as the row does not exist in the database yet
    boolean checkIfExists(final Connection connection, final String table, final Map<String, Object> newObject, final List<String> keys) throws SQLException, ConversionException {
        Map<String, Object> constraints = new LinkedHashMap<>();
        for (String key : keys) {
            if (newObject.containsKey(key)) {
                constraints.put(key, newObject.get(key));
            }
        }
        try {
            if (getObject(connection, table, constraints) != null) { // if an object is found...
                return true;
            }
            return false; // no object is found..
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Insert the provided object into the database
     *
     * @param connection the database connection
     * @param table      the table in which the object will be inserted
     * @param newObject  the objected that will be inserted
     * @throws SQLException
     */
    @SuppressWarnings("squid:S2077")
    // Table name cannot be set with prepared statement.
    void insertObject(final Connection connection, final String table, final LinkedHashMap<String, Object> newObject) throws SQLException {

        List<String> escaped = new ArrayList<>();
        for (String key : newObject.keySet()) {
            escaped.add(escapeIdentifier(key, connection));
        }

        String keyString = StringUtils.join(escaped, ",");

        // Create the same number of prepared statement markers as there are keys
        char[] markers = new char[newObject.size()];
        Arrays.fill(markers, '?');
        String valueString = StringUtils.join(markers, ',');

        String sql = "INSERT INTO " + escapeTableName(table) + " (" + keyString + ") VALUES (" + valueString + ")";

        PreparedStatement statement = connection.prepareStatement(sql);
        fillStatement(newObject, statement, 1);
        statement.execute();
        statement.close();
    }

    /**
     * Update the provided object in the database
     *
     * @param connection the database connection
     * @param table      the table in which an object will be updated
     * @param newObject  the object that will be used to update the table
     * @param keys       the keys for the WHERE part of the query.
     * @throws SQLException
     */
    @SuppressWarnings("squid:S2077")
    // Table name cannot be set with prepared statement.
    void updateObject(final Connection connection, final String table, final LinkedHashMap<String, Object> newObject, final List<String> keys)
            throws SQLException {
        String setString = createQueryPart(connection, newObject.keySet(), ",");
        String whereString = createQueryPart(connection, keys, " AND ");

        String sql = "UPDATE " + escapeTableName(table) + " SET " + setString + " WHERE " + whereString;

        PreparedStatement statement = connection.prepareStatement(sql);
        fillStatement(newObject, statement, 1);

        LinkedHashMap<String, Object> constraintsValues = new LinkedHashMap<>();
        keys.forEach(e -> constraintsValues.put(e, newObject.get(e)));
        fillStatement(constraintsValues, statement, newObject.size() + 1);

        statement.execute();
        statement.close();
    }

    /**
     * Escape the table name.
     *
     * @param table The table name that needs to be escaped
     * @return The escaped table name
     */
    private String escapeTableName(String table) {
        return String.format("`%s`", table);
    }

    /**
     * Fills a {@link PreparedStatement} from a map
     *
     * @param newObject         An ordered map of which keys represent columns and values represent the values for the row in the database
     * @param statement         Prepared statements with as many '?' markers as entries in the newObject map
     * @param firstMarkerNumber The index of the '?' to start setting values
     * @throws SQLException
     */
    void fillStatement(final LinkedHashMap<String, Object> newObject, final PreparedStatement statement, final int firstMarkerNumber) throws SQLException {
        int i = firstMarkerNumber;
        for (Entry<String, Object> e : newObject.entrySet()) {
            setValue(statement, e.getKey(), e.getValue(), i++);
        }
    }

    /**
     * Create a String in this form (where "," is the separator in this case): "key1 = ?,key2 = ?,key3 = ? "
     */
    String createQueryPart(Connection connection, final Iterable<String> keys, final String separator) throws SQLException {
        List<String> escaped = new ArrayList<>();
        for (String identifier : keys) {
            escaped.add(escapeIdentifier(identifier, connection));
        }
        return escaped.stream()
                .map(k -> k + " = ?")
                .reduce((q, k) -> q + separator + k).get();
    }

    String escapeIdentifier(final String identifier, Connection connection) throws SQLException {
        String delimiterString;

        if (!delimiter.containsKey(connection)) {
            delimiter.put(connection, connection.getMetaData().getIdentifierQuoteString());
        }
        delimiterString = delimiter.get(connection);

        return delimiterString + identifier + delimiterString;
    }

    /**
     * Escapes a sql string.
     *
     * @param unescaped The unescaped sql string.
     * @return The escaped sql string.
     */
    @Override
    public String escapeString(String unescaped) {
        String escaped = unescaped;
        escaped = escaped.replaceAll("(\\\\|'|\")", "\\\\$1");
        escaped = escaped.replaceAll("\n", "\\\\n");
        escaped = escaped.replaceAll("\r", "\\\\r");
        escaped = escaped.replaceAll("\t", "\\\\t");
        escaped = escaped.replaceAll("\000", "\\\\000");
        return escaped;
    }
}
