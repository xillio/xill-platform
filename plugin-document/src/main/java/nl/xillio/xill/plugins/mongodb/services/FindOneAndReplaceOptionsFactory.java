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
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Provides a factory method that creates options objects for the {@link nl.xillio.xill.plugins.mongodb.constructs.FindOneAndReplaceConstruct FindOneAndReplaceConstruct}
 *
 * @author Titus Nachbauer
 */
public class FindOneAndReplaceOptionsFactory {
    private final MongoConverter converter;

    @Inject
    FindOneAndReplaceOptionsFactory(MongoConverter converter) {
        this.converter = converter;
    }

    /**
     * Build {@link FindOneAndReplaceOptions} from a MetaExpression.
     *
     * @param argument the expression. This must be an {@link ExpressionDataType#OBJECT}
     * @return the FindOneAndReplaceOptions
     */
    public FindOneAndReplaceOptions build(MetaExpression argument) {
        if (argument.getType() != ExpressionDataType.OBJECT) {
            throw new IllegalArgumentException("The options argument has to be an object");
        }

        Map<String, MetaExpression> options = argument.getValue();

        FindOneAndReplaceOptions result = new FindOneAndReplaceOptions();

        options.forEach((option, value) -> processOption(option, value, result));

        return result;
    }

    private void processOption(String option, MetaExpression value, FindOneAndReplaceOptions options) {
        switch (option) {
            case "projection":
                options.projection(converter.parse(value));
                break;
            case "sort":
                options.sort(converter.parse(value));
                break;
            case "upsert":
                options.upsert(value.getBooleanValue());
                break;
            case "returnNew":
                options.returnDocument(value.getBooleanValue() ? ReturnDocument.AFTER : ReturnDocument.BEFORE);
                break;
            case "maxTime":
                options.maxTime(value.getNumberValue().longValue(), TimeUnit.MILLISECONDS);
                break;
            default:
                throw new RobotRuntimeException("Unknown option `" + option + "`");
        }
    }
}
