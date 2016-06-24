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

import com.google.inject.Inject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import nl.xillio.xill.api.components.MetaExpression;
import org.bson.Document;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for wrapping the functionality of parsing and getting an FindIterable.
 *
 * @author Pieter Dirk Soels
 */
public class FindIterableBuilder extends MongoIterableFactory {

    private MongoConverter mongoConverter;

    @Inject
    void setMongoConverter(MongoConverter mongoConverter) {
        this.mongoConverter = mongoConverter;
    }

    /**
     * This function parses the given arguments to BSON-documents and runs the query on the collection.
     *
     * @param collection the collection of data to execute the query on
     * @param arguments  the parts of the query in this particular order: filter, projection, sort
     * @return the iterable containing the result of the query
     */
    public FindIterable<Document> getIterable(MongoCollection<Document> collection, MetaExpression[] arguments) {
        // Parse the arguments.
        Document filter = mongoConverter.parse(arguments[0]);
        Document projection = mongoConverter.parse(arguments[1]);
        Document sort = mongoConverter.parse(arguments[2]);
        Map<String, MetaExpression> options = arguments[3].getValue();

        // Create the find iterable.
        FindIterable<Document> result = collection.find(filter).projection(projection).sort(sort);

        // Process all options.
        options.forEach((option, value) -> processOption(option, value, result));

        return result;
    }

    private void processOption(String option, MetaExpression value, FindIterable<Document> iterable) {
        switch (option) {
            case "limit":
                iterable.limit(value.getNumberValue().intValue());
                break;
            case "skip":
                iterable.skip(value.getNumberValue().intValue());
                break;
            case "maxTime":
                iterable.maxTime(value.getNumberValue().longValue(), TimeUnit.MILLISECONDS);
                break;
            case "noCursorTimeout":
                iterable.noCursorTimeout(value.getBooleanValue());
                break;
            default:
                super.processOption(option, value, iterable);
        }
    }
}
