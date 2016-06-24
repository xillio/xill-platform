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
import com.mongodb.client.model.InsertManyOptions;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;

/**
 * This class is responsible for converting a {@link MetaExpression} to {@link InsertManyOptions}.
 *
 * @author Titus Nachbauer
 */
public class InsertManyOptionsFactory {
    private final MongoConverter converter;

    @Inject
    InsertManyOptionsFactory(MongoConverter converter) {
        this.converter = converter;
    }

    /**
     * Build {@link InsertManyOptions} from a MetaExpression.
     *
     * @param argument the expression. This must be an {@link ExpressionDataType#OBJECT}
     * @return the InsertManyOptions
     */
    public InsertManyOptions build(MetaExpression argument) {
        if (argument.getType() != ExpressionDataType.ATOMIC) {
            throw new IllegalArgumentException("The options argument has to be an ATOMIC");
        }

        InsertManyOptions result = new InsertManyOptions();

        processOption("ordered", argument, result);

        return result;
    }

    private void processOption(String option, MetaExpression value, InsertManyOptions options) {
        switch (option) {
            case "ordered":
                options.ordered(value.getBooleanValue());
                break;
            default:
                throw new RobotRuntimeException(String.format("Unknown insert option: %s", option));
        }
    }
}
