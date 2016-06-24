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
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.RenameCollectionOptions;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.mongodb.services.RenameCollectionOptionsFactory;
import org.bson.Document;

/**
 * This construct represents the renameCollection method on MongoDB.
 *
 * @author Titus Nachbauer
 * @see <a href="https://docs.mongodb.org/v3.0/reference/method/db.collection.renamecollection/#db.collection.renamecollection">db.collection.renamecollection</a>
 */
public class RenameCollectionConstruct extends AbstractCollectionApiConstruct {
    private final RenameCollectionOptionsFactory renameCollectionOptionsFactory;

    @Inject
    public RenameCollectionConstruct(RenameCollectionOptionsFactory renameCollectionOptionsFactory) {
        this.renameCollectionOptionsFactory = renameCollectionOptionsFactory;
    }

    @Override
    protected Argument[] getApiArguments() {
        return new Argument[]{
                new Argument("target", emptyObject(), ATOMIC),
                new Argument("dropTarget", emptyObject(), ATOMIC)
        };
    }

    @Override
    MetaExpression process(MetaExpression[] arguments, MongoCollection<Document> collection, ConstructContext context) {
        MongoNamespace target = new MongoNamespace(collection.getNamespace().getDatabaseName() + "." + arguments[0].getStringValue());
        RenameCollectionOptions options = renameCollectionOptionsFactory.build(arguments[1]);

        tryRenameCollectionMany(collection, target, options);
        return NULL;
    }

    private void tryRenameCollectionMany(MongoCollection<Document> collection, MongoNamespace target, RenameCollectionOptions options) {
        try {
            collection.renameCollection(target, options);
        } catch (com.mongodb.MongoException e) {
            throw new RobotRuntimeException("RenameCollection failed: " + e.getMessage(), e);
        }
    }
}
