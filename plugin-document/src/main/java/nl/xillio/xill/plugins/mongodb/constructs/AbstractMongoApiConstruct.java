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
package nl.xillio.xill.plugins.mongodb.constructs;

import com.google.inject.Inject;
import com.mongodb.MongoQueryException;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.MetaExpressionIterator;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.mongodb.NoSuchConnectionException;
import nl.xillio.xill.plugins.mongodb.services.BsonValueConverter;
import nl.xillio.xill.plugins.mongodb.services.Connection;
import nl.xillio.xill.plugins.mongodb.services.ConnectionManager;
import nl.xillio.xill.plugins.mongodb.services.MongoConverter;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonValue;
import org.bson.Document;

/**
 * A convenience class to group some useful methods on Mongo databases and facilitate Mongo constructs development.
 * It will enforce the expected signature of constructs interacting directly with the database,
 * as opposed to a specific collection in the database, to have one optional parameter, at the
 * end of the parameter list, to specify which connection to use for the requested operation.
 *
 * @author Andrea Parrilli
 */
abstract class AbstractMongoApiConstruct extends Construct {
    protected ConnectionManager connectionManager;
    protected MongoConverter mongoConverter;
    protected BsonValueConverter bsonValueConverter;

    @Inject
    void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Inject
    void setMongoConverter(MongoConverter mongoConverter) {
        this.mongoConverter = mongoConverter;
    }

    @Inject
    void setBsonValueConverter(BsonValueConverter bsonValueConverter) {
        this.bsonValueConverter = bsonValueConverter;
    }

    protected Connection getConnection(ConstructContext context, MetaExpression connectionExpression) {
        if (connectionExpression.isNull()) {
            return getConnection(context);
        }

        Connection connection = connectionExpression.getMeta(Connection.class);
        if (connection == null) {
            throw new RobotRuntimeException("The passed database parameter is not a valid MongoDB database. Please connect using the Mongo.connect construct");
        }
        return connection;
    }

    private Connection getConnection(ConstructContext context) {
        try {
            return connectionManager.getConnection(context);
        } catch (NoSuchConnectionException e) {
            throw new RobotRuntimeException("No active connection found for this robot. Please use Mongo.connect to create a connection", e);
        }
    }

    protected Document toDocument(MetaExpression query) {
        return mongoConverter.parse(query);
    }

    protected MetaExpression toExpression(Document document) {
        return mongoConverter.parse(document);
    }

    /**
     * Create a result expression from a MongoIterable.
     *
     * @param source     the iterable
     * @param collection the Mongo collection to use as namespace
     * @param arguments  the arguments that should be included in the string representation
     * @return the expression
     */
    protected MetaExpression fromValue(MongoIterable<Document> source, MongoCollection<Document> collection, MetaExpression... arguments) {

        MetaExpression result = fromValue(String.format("db.%s.%s(%s)", collection.getNamespace().getCollectionName(), getName(), StringUtils.join(arguments, ",")));
        result.storeMeta(new MetaExpressionIterator<>(
                source.iterator(),
                this::toExpression
        ));

        return result;
    }

    /**
     * Create a result expression from a MongoIterable.
     *
     * @param source     the iterable
     * @param collection the Mongo collection to use as namespace
     * @param arguments  the arguments that should be included in the string representation
     * @return the expression
     */
    protected MetaExpression fromValueRaw(MongoIterable<BsonValue> source, MongoCollection<Document> collection, MetaExpression... arguments) {

        MetaExpression result = fromValue(String.format("db.%s.%s(%s)", collection.getNamespace().getCollectionName(), getName(), StringUtils.join(arguments, ",")));
        result.storeMeta(new MetaExpressionIterator<>(
                source.iterator(),
                bsonValueConverter::convert
        ));

        return result;
    }


    /**
     * Adds the default database connection parameter to the list of construct arguments,
     * thus enforcing the presence of the optional database connection parameter as last argument
     * of any Mongo construct call.
     */
    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        Argument[] apiArguments = getApiArguments();
        Argument[] arguments = new Argument[apiArguments.length + 1];
        arguments[arguments.length - 1] = new Argument("database", NULL, ATOMIC);

        System.arraycopy(apiArguments, 0, arguments, 0, arguments.length - 1);

        return new ConstructProcessor(
                args -> process(args, context),
                arguments
        );
    }

    MetaExpression process(MetaExpression[] arguments, ConstructContext context) {
        // Parse arguments
        MetaExpression[] customArguments = new MetaExpression[arguments.length - 1];
        System.arraycopy(arguments, 0, customArguments, 0, arguments.length - 1);

        // Fetch Connection
        Connection connection = getConnection(context, arguments[arguments.length - 1]);

        try {
            return process(customArguments, connection, context);
        } catch (MongoQueryException e) {
            throw new RobotRuntimeException("Could not parse query: " + e.getErrorMessage(), e);
        } catch (MongoSocketOpenException e) {
            throw new RobotRuntimeException("Could not connect to the database at " + e.getServerAddress() + "\nError: " + e.getMessage(), e);
        }
    }

    /**
     * Implement this method to define the arguments taken by the concrete MongoConstruct.
     * The database connection will be appended last by default.
     *
     * @return The custom, modeled arguments of the construct.
     */
    abstract Argument[] getApiArguments();

    /**
     * Implement here your construct logic. Note the connection is passed as a distinct argument.
     *
     * @param arguments  The custom arguments to pass to the construct
     * @param connection The Mongo connection to be used in the construct processing
     * @param context    The usual context of the construct
     * @return The result of the construct processing
     */
    abstract MetaExpression process(MetaExpression[] arguments, Connection connection, ConstructContext context);
}
