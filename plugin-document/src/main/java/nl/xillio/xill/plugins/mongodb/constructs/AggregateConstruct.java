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
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.plugins.mongodb.services.AggregateIterableFactory;
import org.bson.Document;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This construct represents the aggregate method on MongoDB.
 *
 * @author Thomas Biesaart
 * @see <a href="https://docs.mongodb.org/v3.0/reference/method/db.collection.aggregate/#db.collection.aggregate">db.collection.aggregate</a>
 */
public class AggregateConstruct extends AbstractCollectionApiConstruct {

    private AggregateIterableFactory aggregateIterableFactory;

    @Inject
    public AggregateConstruct(AggregateIterableFactory aggregateIterableFactory) {
        this.aggregateIterableFactory = aggregateIterableFactory;
    }

    @Override
    @SuppressWarnings("squid:S2095")
    protected Argument[] getApiArguments() {
        return new Argument[]{
                new Argument("pipeline", LIST),
                new Argument("options", emptyObject(), OBJECT)
        };
    }

    @Override
    MetaExpression process(MetaExpression[] arguments, MongoCollection<Document> collection, ConstructContext context) {
        List<Document> pipeline = arguments[0].<List<MetaExpression>>getValue().stream()
                .map(this::toDocument)
                .collect(Collectors.toList());

        AggregateIterable<Document> mongoResult = aggregateIterableFactory.aggregate(collection, pipeline, arguments[1]);

        return fromValue(mongoResult, collection, arguments[0]);
    }
}
