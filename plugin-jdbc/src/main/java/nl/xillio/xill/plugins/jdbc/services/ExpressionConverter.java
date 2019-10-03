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
package nl.xillio.xill.plugins.jdbc.services;

import com.google.inject.Singleton;
import me.biesaart.utils.Log;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.io.IOStream;
import nl.xillio.xill.api.io.SimpleIOStream;
import nl.xillio.xill.plugins.jdbc.data.TemporalMetadataExpression;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;
import static nl.xillio.xill.api.components.MetaExpression.extractValue;
import static nl.xillio.xill.api.components.MetaExpression.parseObject;

/**
 * This class is responsible for converting jdbc objects to and from MetaExpressions.
 *
 * @author Thomas Biesaart
 */
@Singleton
public class ExpressionConverter {
    private static final Logger LOGGER = Log.get();

    /**
     * Parse all labels from the current row from a result set to a {@link nl.xillio.xill.api.components.ExpressionDataType#OBJECT}.
     * This is equivalent to calling: <code>parseRow(resultSet, resultSet.getMetaData())</code>
     *
     * @param resultSet the result set
     * @return the object
     * @throws SQLException if a database access error occurs or this method is called on a closed result set
     */
    public MetaExpression parseRow(ResultSet resultSet) throws SQLException {
        return parseRow(resultSet, resultSet.getMetaData());
    }

    /**
     * Parse all labels from the current row from a result set to a {@link nl.xillio.xill.api.components.ExpressionDataType#OBJECT}.
     *
     * @param resultSet the result set
     * @param metaData  the result set metadata
     * @return the object
     * @throws SQLException if a database access error occurs or this method is called on a closed result set
     */
    public MetaExpression parseRow(ResultSet resultSet, ResultSetMetaData metaData) throws SQLException {
        int count = metaData.getColumnCount();
        List<String> labels = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            labels.add(metaData.getColumnLabel(i));
        }

        return parseRow(resultSet, labels);
    }

    /**
     * Parse a set of labels from the current row from a result set to a {@link nl.xillio.xill.api.components.ExpressionDataType#OBJECT}.
     *
     * @param resultSet the result set
     * @param labels    the labels the should be parsed
     * @return the object
     * @throws SQLException if a database access error occurs or this method is called on a closed result set
     */
    public MetaExpression parseRow(ResultSet resultSet, List<String> labels) throws SQLException {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();
        for (String label : labels) {
            Object value = resultSet.getObject(label);
            MetaExpression expression = parseExpression(value);
            result.put(label, expression);
        }
        return fromValue(result);
    }

    /**
     * Parse an object to a MetaExpression with support for byte[].
     *
     * @param input the input object
     * @return the MetaExpression
     */
    public MetaExpression parseExpression(Object input) {
        if (input instanceof byte[]) {
            IOStream ioStream = new SimpleIOStream(
                    new ByteArrayInputStream((byte[]) input),
                    "SQL Binary"
            );
            return fromValue(ioStream);
        }

        if (input instanceof Clob) {
            InputStream inputStream = getInputStream((Clob) input);
            IOStream ioStream = new SimpleIOStream(
                    inputStream,
                    "SQL Long Text"
            );
            return fromValue(ioStream);
        }

        if (input instanceof Blob) {
            InputStream inputStream = getInputStream((Blob) input);
            IOStream ioStream = new SimpleIOStream(
                    inputStream,
                    "SQL Blob"
            );
            return fromValue(ioStream);
        }
        return parseObject(input);
    }

    private InputStream getInputStream(Clob clob) {
        try {
            return clob.getAsciiStream();
        } catch (SQLException e) {
            throw new RobotRuntimeException("Could not read text from stream: " + e.getMessage(), e);
        }
    }

    private InputStream getInputStream(Blob blob) {
        try {
            return blob.getBinaryStream();
        } catch (SQLException e) {
            throw new RobotRuntimeException("Could not read text from stream: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts and returns an Object from a MetaExpression containing a binary input or output stream, returns null if it does not contain either.
     *
     * @param expression the MetaExpression containing the binary Object
     * @return the Object or null
     * @see nl.xillio.xill.api.components.MetaExpressionDeserializer
     */
    public Object extract(MetaExpression expression) {
        return extractValue(expression, this::extractBinary);
    }

    public Map<String, Object> extractFromObject(MetaExpression object) {
        Map<String, MetaExpression> mapMeta = object.getValue();
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, MetaExpression> entry : mapMeta.entrySet()) {
            Object value;

            if (entry.getValue().getMeta(TemporalMetadataExpression.class) != null) {
                value = entry.getValue().getMeta(TemporalMetadataExpression.class).getData();
            } else {
                value = extractValue(entry.getValue(), this::extractBinary);
            }

            result.put(entry.getKey(), value);
        }

        return result;
    }

    @SuppressWarnings("squid:UnusedPrivateMethod") // Sonar does not recognize method references
    private Object extractBinary(MetaExpression expression) {
        if (expression.getBinaryValue().hasInputStream()) {
            try {
                return expression.getBinaryValue().getInputStream();
            } catch (IOException e) {
                LOGGER.error("Failed to get input stream", e);
            }
        }

        if (expression.getBinaryValue().hasOutputStream()) {
            try {
                return expression.getBinaryValue().getOutputStream();
            } catch (IOException e) {
                LOGGER.error("Failed to get input stream", e);
            }
        }

        return null;
    }

    /**
     * Parses a {@link nl.xillio.xill.api.components.ExpressionDataType#OBJECT} and returns a Map of constraints for a query.
     * This Map contains column names and the values they should match.
     *
     * @param constraintsObject the MetaExpression containing the constraints
     * @return the Map of constraints
     */
    public Map<String, Object> parseConstraints(MetaExpression constraintsObject) {
        Map<String, MetaExpression> expressionMap = constraintsObject.getValue();

        return expressionMap.entrySet().stream()
                .filter(e -> !e.getValue().isNull())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> extract(e.getValue())
                ));
    }
}
