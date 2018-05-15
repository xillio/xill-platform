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
package nl.xillio.xill.plugins.mongodb.services;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for creating a AggregateIterable with specified options
 *
 * @author Edward van Egdom
 */
public class AggregateIterableFactory extends MongoIterableFactory {
    public AggregateIterable<Document> aggregate(MongoCollection<Document> collection, List<Document> pipeline, MetaExpression optionsVar) {
        if (optionsVar.getType() != ExpressionDataType.OBJECT) {
            throw new IllegalStateException("Options argument is not an object");
        }

        AggregateIterable<Document> aggregateIterable = collection.aggregate(pipeline);

        // Process options
        Map<String, MetaExpression> options = optionsVar.getValue();
        options.forEach((option, value) -> processOption(option, value, aggregateIterable));

        return aggregateIterable;
    }

    public void processOption(String option, MetaExpression value, AggregateIterable<Document> iterable) {
        switch (option) {
            case "allowDiskUsage":
                iterable.allowDiskUse(value.getBooleanValue());
                break;
             case "allowDiskUse" :
                 iterable.allowDiskUse(value.getBooleanValue());
                 break;
            case "maxTime":
                iterable.maxTime(value.getNumberValue().longValue(), TimeUnit.MILLISECONDS);
                break;
            default:
                super.processOption(option, value, iterable);
        }
    }
}