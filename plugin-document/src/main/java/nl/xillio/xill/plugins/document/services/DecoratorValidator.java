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
package nl.xillio.xill.plugins.document.services;

import com.google.inject.Singleton;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.util.Map;

/**
 * This class is responsible for validating decorator definitions.
 *
 * @author Thomas Biesaart
 */
@Singleton
public class DecoratorValidator {

    public void validate(MetaExpression expression) {
        if (expression.getType() != ExpressionDataType.OBJECT) {
            throw new RobotRuntimeException("A decorator definition must be an OBJECT");
        }

        Map<String, MetaExpression> map = expression.getValue();
        map.forEach((name, field) -> {
            validateFieldName(name);
            validateField(name, field);
        });
    }

    private void validateFieldName(String name) {
        if (name.trim().isEmpty()) {
            throw new RobotRuntimeException("Field names cannot be empty");
        }
        if (name.startsWith("$")) {
            throw new RobotRuntimeException("Field names cannot start with '$'");
        }
        if (name.contains(".")) {
            throw new RobotRuntimeException("Field names cannot contain '.'");
        }
    }

    private void validateField(String name, MetaExpression field) {
        if (field.getType() != ExpressionDataType.OBJECT) {
            throw new RobotRuntimeException("A field definition must be an OBJECT");
        }

        Map<String, MetaExpression> map = field.getValue();

        boolean foundType = false;
        for (String key : map.keySet()) {
            if ("type".equals(key)) {
                validateFieldType(map.get(key).getStringValue());
                foundType = true;
            } else if (!"required".equals(key)) {
                throw new RobotRuntimeException("Unknown option ` " + key + "` for field `" + name + "`");
            }
        }
        if(!foundType) {
            throw new RobotRuntimeException("Please provide a type for field `" + name + "`");
        }
    }

    private void validateFieldType(String fieldType) {
        try {
            FieldType.valueOf(fieldType);
        } catch (IllegalArgumentException e) {
            throw new RobotRuntimeException(
                    "Invalid field type `" + fieldType + "`." +
                            "Available types: " + FieldType.AVAILABLE_TYPES,
                    e
            );
        }
    }

}
