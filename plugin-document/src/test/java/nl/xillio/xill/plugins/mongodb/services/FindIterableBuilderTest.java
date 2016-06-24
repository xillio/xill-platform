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

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import org.bson.Document;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

public class FindIterableBuilderTest extends TestUtils {
    @Test
    public void testFindIterableOptions() {
        // Builder and options.
        FindIterableBuilder builder = new FindIterableBuilder();
        LinkedHashMap<String, MetaExpression> options = new LinkedHashMap<>();

        // Mock arguments.
        MetaExpression[] args = new MetaExpression[]{emptyObject(), emptyObject(), emptyObject(), fromValue(options)};
        MongoConverter converter = mock(MongoConverter.class);
        Document emptyDoc = mock(Document.class);
        when(converter.parse(emptyObject())).thenReturn(emptyDoc);
        builder.setMongoConverter(converter);

        // Mock collection and its methods.
        MongoCollection<Document> collection = mock(MongoCollection.class);
        FindIterable<Document> iterable = mock(FindIterable.class);
        when(collection.find(emptyDoc)).thenReturn(iterable);
        when(iterable.projection(emptyDoc)).thenReturn(iterable);
        when(iterable.sort(emptyDoc)).thenReturn(iterable);

        // Initial test.
        FindIterable<Document> result = builder.getIterable(collection, args);
        assertSame(result, iterable);

        // batchSize
        options.put("batchSize", fromValue(1));
        result = builder.getIterable(collection, args);
        verify(result).batchSize(1);

        // limit
        options.put("limit", fromValue(2));
        result = builder.getIterable(collection, args);
        verify(result).limit(2);

        // skip
        options.put("skip", fromValue(3));
        result = builder.getIterable(collection, args);
        verify(result).skip(3);

        // maxTime
        options.put("maxTime", fromValue(5));
        result = builder.getIterable(collection, args);
        verify(result).maxTime(5, TimeUnit.MILLISECONDS);

        // noCursorTimeout
        options.put("noCursorTimeout", fromValue(true));
        result = builder.getIterable(collection, args);
        verify(result).noCursorTimeout(true);
    }
}