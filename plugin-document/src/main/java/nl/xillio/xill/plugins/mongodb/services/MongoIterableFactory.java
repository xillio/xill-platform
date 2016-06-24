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

import com.mongodb.client.MongoIterable;
import nl.xillio.xill.api.components.MetaExpression;
import org.bson.Document;

/**
 * Class responsible for creating a MongoIterable with specified options.
 *
 * @author Edward van Egdom
 */
public class MongoIterableFactory {
    public void processOption(String option, MetaExpression value, MongoIterable<Document> iterable) {
        switch (option) {
            case "batchSize":
                iterable.batchSize(value.getNumberValue().intValue());
                break;
        }
    }
}
