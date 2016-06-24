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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.mongodb.data.MongoObjectId;

/**
 * Creates a Mongo ObjectId from a 24 character hexadecimal string
 *
 * @author Titus Nachbauer
 */
public class ObjectIdConstruct extends Construct {

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(this::process, new Argument("string", ATOMIC));
    }

    @SuppressWarnings("squid:UnusedPrivateMethod") // Sonar does not do method references
    private MetaExpression process(MetaExpression string) {
        MetaExpression result = fromValue(string.getStringValue());
        MongoObjectId objectId = new MongoObjectId(result.getStringValue());
        result.storeMeta(objectId);
        return result;
    }
}
