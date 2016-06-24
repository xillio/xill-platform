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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

/**
 * Created by andrea.parrilli on 2016-05-19.
 */
public class DecoratorValidatorTest extends TestUtils {
    private LinkedHashMap<String, MetaExpression> makeDecoratorMap(String fieldName, FieldType type, boolean required, LinkedHashMap<String, MetaExpression> appendTo) {
        LinkedHashMap<String, MetaExpression> decoratorFieldMap = new LinkedHashMap<>();
        decoratorFieldMap.put("type", fromValue(type.name()));
        decoratorFieldMap.put("required", fromValue(required));

        LinkedHashMap<String, MetaExpression> decoratorMap = (appendTo == null ? new LinkedHashMap<>() : appendTo);
        decoratorMap.put(fieldName, fromValue(decoratorFieldMap));

        return decoratorMap;
    }

    @Test
    public void validDecoratorTest() {
        DecoratorValidator validator = new DecoratorValidator();

        validator.validate(fromValue(makeDecoratorMap("AValidFieldName", FieldType.STRING, true, null)));
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void invalidFieldNameTestEmpty() {
        DecoratorValidator validator = new DecoratorValidator();

        String invalidName = "";
        validator.validate(fromValue(makeDecoratorMap(invalidName, FieldType.STRING, true, null)));
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void invalidFieldNameTestBlank() {
        DecoratorValidator validator = new DecoratorValidator();

        String invalidName = "     ";
        validator.validate(fromValue(makeDecoratorMap(invalidName, FieldType.STRING, true, null)));
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void invalidFieldNameTestSpecialChars1() {
        DecoratorValidator validator = new DecoratorValidator();

        String invalidName = "$";
        validator.validate(fromValue(makeDecoratorMap(invalidName, FieldType.STRING, true, null)));
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void invalidFieldNameTestSpecialChars2() {
        DecoratorValidator validator = new DecoratorValidator();

        String invalidName = ".";
        validator.validate(fromValue(makeDecoratorMap(invalidName, FieldType.STRING, true, null)));
    }
}
