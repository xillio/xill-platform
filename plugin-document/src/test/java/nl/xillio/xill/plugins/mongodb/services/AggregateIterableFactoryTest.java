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
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import org.bson.Document;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

/**
 * @author Edward van Egdom
 */
@SuppressWarnings("unchecked")
public class AggregateIterableFactoryTest extends TestUtils {
    @Test
    public void testAggregateIterableOptions() {
        // Test adding allowDiskUsage option
        AggregateIterableFactory aggregateIterableFactory = new AggregateIterableFactory();
        LinkedHashMap<String, MetaExpression> object = new LinkedHashMap<>();
        object.put("allowDiskUsage", fromValue(true));

        AggregateIterable iterable = mock(AggregateIterable.class);

        MongoCollection<Document> collection = mock(MongoCollection.class);
        List<Document> pipeline = mock(List.class);
        when(collection.aggregate(pipeline)).thenReturn(iterable);

        AggregateIterable<Document> aggregateIterable = aggregateIterableFactory.aggregate(collection, pipeline, fromValue(object));

        assertSame(aggregateIterable, iterable);
        verify(iterable).allowDiskUse(true);

        // Adding batchSize option
        object.put("batchSize", fromValue(2));
        aggregateIterable = aggregateIterableFactory.aggregate(collection, pipeline, fromValue(object));
        verify(aggregateIterable).batchSize(2);

        // Adding maxTime option
        object.put("maxTime", fromValue(1));
        aggregateIterable = aggregateIterableFactory.aggregate(collection, pipeline, fromValue(object));
        verify(aggregateIterable).maxTime(1, TimeUnit.MILLISECONDS);
    }
}