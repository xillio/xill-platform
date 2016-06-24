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
import com.google.inject.Singleton;
import com.mongodb.client.model.IndexOptions;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for converting a {@link MetaExpression} to {@link IndexOptions}.
 *
 * @author Thomas Biesaart
 */
@Singleton
public class IndexOptionsFactory {

    private final MongoConverter converter;

    @Inject
    IndexOptionsFactory(MongoConverter converter) {
        this.converter = converter;
    }

    /**
     * Build IndexOptions from a MetaExpression.
     *
     * @param argument the expression. This must be an {@link ExpressionDataType#OBJECT}
     * @return the IndexOptions
     */
    public IndexOptions build(MetaExpression argument) {
        if (argument.getType() != ExpressionDataType.OBJECT) {
            throw new IllegalArgumentException("The options argument has to be an object");
        }

        Map<String, MetaExpression> options = argument.getValue();

        IndexOptions result = new IndexOptions();

        options.forEach((option, value) -> processOption(option, value, result));

        return result;
    }

    private void processOption(String option, MetaExpression value, IndexOptions options) {
        switch (option) {
            case "background":
                options.background(value.getBooleanValue());
                break;
            case "unique":
                options.unique(value.getBooleanValue());
                break;
            case "name":
                options.name(value.getStringValue());
                break;
            case "sparse":
                options.sparse(value.getBooleanValue());
                break;
            case "expireAfterSeconds":
                options.expireAfter(value.getNumberValue().longValue(), TimeUnit.SECONDS);
                break;
            case "v":
                options.version(value.getNumberValue().intValue());
                break;
            case "storageEngine":
                options.storageEngine(converter.parse(value));
                break;
            case "weights":
                options.weights(converter.parse(value));
                break;
            case "default_language":
                options.defaultLanguage(value.getStringValue());
                break;
            case "language_override":
                options.languageOverride(value.getStringValue());
                break;
            case "textIndexVersion":
                options.textVersion(value.getNumberValue().intValue());
                break;
            case "2dsphereIndexVersion":
                options.sphereVersion(value.getNumberValue().intValue());
                break;
            case "bits":
                options.bits(value.getNumberValue().intValue());
                break;
            case "min":
                options.min(value.getNumberValue().doubleValue());
                break;
            case "max":
                options.max(value.getNumberValue().doubleValue());
                break;
            case "bucketSize":
                options.bucketSize(value.getNumberValue().doubleValue());
                break;
            default:
                throw new RobotRuntimeException(String.format("Unknown index option: %s", option));
        }
    }
}
