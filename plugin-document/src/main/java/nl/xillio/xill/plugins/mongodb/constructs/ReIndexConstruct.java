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

import com.mongodb.MongoCommandException;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.mongodb.services.Connection;
import org.bson.BsonDocument;
import org.bson.BsonString;

/**
 * This construct represents the reIndex function in MongoDB.
 *
 * @author Luca Scalzotto
 * @see <a href="https://docs.mongodb.com/manual/reference/command/reIndex/">db.collection.reIndex()</a>
 */
public class ReIndexConstruct extends AbstractMongoApiConstruct {

    @Override
    protected Argument[] getApiArguments() {
        return new Argument[]{
                new Argument("collection", ATOMIC)
        };
    }

    @Override
    MetaExpression process(MetaExpression[] arguments, Connection connection, ConstructContext context) {
        String collection = arguments[0].getStringValue();

        // Build the command.
        BsonDocument command = new BsonDocument();
        command.put("reIndex", new BsonString(collection));

        // Run the command and return the result.
        try {
            return toExpression(connection.getDatabase().runCommand(command));
        } catch (MongoCommandException e) {
            throw new RobotRuntimeException("Failed to reIndex \"" + collection + "\", error: " + e.getErrorMessage(), e);
        }
    }
}
