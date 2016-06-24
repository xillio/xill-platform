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

import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.MetaExpressionIterator;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.plugins.mongodb.services.Connection;

import java.util.Iterator;

/**
 * This construct represents the getCollectionNames method on the database object in MongoDB.
 * The API does not have any argument.
 *
 * @author Andrea Parrilli
 * @see <a href="https://docs.mongodb.org/manual/reference/command/listCollections/">db.listCollections()</a>
 */
public class GetCollectionNamesConstruct extends AbstractMongoApiConstruct {

    // This construct only takes the default database connection argument
    @Override
    protected Argument[] getApiArguments() {
        return new Argument[0];
    }

    @Override
    MetaExpression process(MetaExpression[] arguments, Connection connection, ConstructContext context) {
        Iterator<String> collections = connection.getDatabase().listCollectionNames().iterator();
        MetaExpressionIterator<String> iterator = new MetaExpressionIterator<>(collections, ExpressionBuilderHelper::fromValue);

        MetaExpression result = fromValue("db.getCollectionNames() in " + connection.toString());
        result.storeMeta(iterator);

        return result;
    }
}
