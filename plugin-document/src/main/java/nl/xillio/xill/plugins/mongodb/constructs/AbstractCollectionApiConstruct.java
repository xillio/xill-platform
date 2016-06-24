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

import com.mongodb.MongoQueryException;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.client.MongoCollection;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.mongodb.services.Connection;
import org.bson.Document;

/**
 * This abstract class supports the development of collection constructs for MongoDB.
 * It inherits some convenience methods from is superclass, but enforces a different signature for derived Constructs:
 * to the API modeled arguments it prepends a "collection name" first parameter and an optional last parameter for the
 * database connection to use.
 *
 * @author Thomas Biesaart
 * @author Andrea Parrilli
 */
abstract class AbstractCollectionApiConstruct extends AbstractMongoApiConstruct {

    abstract MetaExpression process(MetaExpression[] arguments, MongoCollection<Document> collection, ConstructContext context);

    /**
     * This method, although abstract in the superclass, must not be implemented, that is because
     * in this class, the default expected signature of a construct features a first argument for the collection name
     * and a last optional one for the connection to use, which is different from this method's signature.
     *
     * @param arguments  The custom arguments to pass to the construct
     * @param connection The Mongo connection to be used in the construct processing
     * @param context    The usual context of the construct
     * @return
     */
    @Override
    final MetaExpression process(MetaExpression[] arguments, Connection connection, ConstructContext context) {
        throw new UnsupportedOperationException("This should never be called: please only invoke the process(MetaExpression[], MongoCollection<Document>, ConstructContext) in this class");
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        Argument[] apiArguments = getApiArguments();
        Argument[] arguments = new Argument[apiArguments.length + 2];

        arguments[0] = new Argument("collectionName", ATOMIC);
        arguments[arguments.length - 1] = new Argument("database", NULL, ATOMIC);

        System.arraycopy(apiArguments, 0, arguments, 1, arguments.length - 2);

        return new ConstructProcessor(
                args -> process(args, context),
                arguments
        );
    }

    @Override
    MetaExpression process(MetaExpression[] arguments, ConstructContext context) {
        // Parse arguments
        MetaExpression[] customArguments = new MetaExpression[arguments.length - 2];
        System.arraycopy(arguments, 1, customArguments, 0, arguments.length - 2);

        // Fetch Connection
        Connection connection = getConnection(context, arguments[arguments.length - 1]);

        // Fetch Collection
        String collectionName = arguments[0].getStringValue();
        MongoCollection<Document> collection = connection.getDatabase().getCollection(collectionName);

        try {
            return process(customArguments, collection, context);
        } catch (MongoQueryException e) {
            throw new RobotRuntimeException("Could not parse query: " + e.getErrorMessage(), e);
        } catch (MongoSocketOpenException e) {
            throw new RobotRuntimeException("Could not connect to the database at " + e.getServerAddress() + "\nError: " + e.getMessage(), e);
        }
    }


}
