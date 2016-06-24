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
import com.mongodb.client.model.RenameCollectionOptions;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;

/**
 * Provides a factory method that creates options objects for the {@link nl.xillio.xill.plugins.mongodb.constructs.FindOneAndUpdateConstruct FindOneAndUpdateConstruct}
 *
 * @author Titus Nachbauer
 */
public class RenameCollectionOptionsFactory {
    private final MongoConverter converter;

    @Inject
    RenameCollectionOptionsFactory(MongoConverter converter) {
        this.converter = converter;
    }

    /**
     * Build {@link RenameCollectionOptions} from a MetaExpression.
     *
     * @param argument the expression. This must be an {@link ExpressionDataType#OBJECT}
     * @return the RenameCollectionOptions
     */
    public RenameCollectionOptions build(MetaExpression argument) {
        if (argument.getType() != ExpressionDataType.ATOMIC) {
            throw new IllegalArgumentException("The options argument has to be an ATOMIC");
        }

        RenameCollectionOptions result = new RenameCollectionOptions();

        processOption("dropTarget", argument, result);

        return result;
    }

    private void processOption(String option, MetaExpression value, RenameCollectionOptions options) {
        switch (option) {
            case "dropTarget":
                options.dropTarget(value.getBooleanValue());
                break;
            default:
                throw new RobotRuntimeException(String.format("Unknown rename option: %s", option));
        }
    }
}
