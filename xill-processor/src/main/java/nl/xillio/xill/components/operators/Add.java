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
package nl.xillio.xill.components.operators;

import nl.xillio.util.MathUtils;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.*;
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;

/**
 * This class represents the + operation.
 */
public final class Add extends BinaryNumberOperator {

    public Add(final Processable left, final Processable right) {
        super(left, right, MathUtils::add);
    }

    @SuppressWarnings("unchecked")
    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) {
        MetaExpression leftValue = left.process(debugger).get();
        MetaExpression rightValue = right.process(debugger).get();

        leftValue.registerReference();
        rightValue.registerReference();

        try {
            // If both entries are a list, then add them as such
            if (leftValue.getType() == rightValue.getType() && leftValue.getType() == ExpressionDataType.LIST) {
                return InstructionFlow.doResume(
                        processLists((List<MetaExpression>) leftValue.getValue(),
                                (List<MetaExpression>) rightValue.getValue()));
            }

            // If both entries are an object, then add them as such
            if (leftValue.getType() == rightValue.getType() && leftValue.getType() == ExpressionDataType.OBJECT) {
                return InstructionFlow.doResume(
                        processObjects((LinkedHashMap<String, MetaExpression>) leftValue.getValue(),
                                (LinkedHashMap<String, MetaExpression>) rightValue.getValue()));
            }

            // If the left entry is a list and the right entry is an object,
            // then convert the list to an object and put it in front of the object.
            if (leftValue.getType() == ExpressionDataType.LIST && rightValue.getType() == ExpressionDataType.OBJECT) {
                return InstructionFlow.doResume(
                        processListObject((List<MetaExpression>) leftValue.getValue(),
                                (LinkedHashMap<String, MetaExpression>) rightValue.getValue()));
            }

            // If the left entry is an object and the right entry is a list,
            // then convert the list to an object and put it at the end of the object.
            if (leftValue.getType() == ExpressionDataType.OBJECT && rightValue.getType() == ExpressionDataType.LIST) {
                return InstructionFlow.doResume(
                        processObjectList((LinkedHashMap<String, MetaExpression>) leftValue.getValue(),
                                (List<MetaExpression>) rightValue.getValue()));
            }

            // The left and right entries are atomics so add them as numbers.
            return super.process(leftValue, rightValue);
        } finally {
            rightValue.releaseReference();
            leftValue.releaseReference();
        }

    }

    private static MetaExpression processLists(final List<MetaExpression> leftValue, final List<MetaExpression> rightValue) {
        List<MetaExpression> result = new ArrayList<>(leftValue);
        result.addAll(rightValue);

        return fromValue(result);
    }

    private static MetaExpression processObjects(final LinkedHashMap<String, MetaExpression> leftValue, final LinkedHashMap<String, MetaExpression> rightValue) {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>(leftValue);
        result.putAll(rightValue);

        return fromValue(result);
    }

    private static MetaExpression processListObject(final List<MetaExpression> leftValue, final LinkedHashMap<String, MetaExpression> rightValue) {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();
        for (int i = 0; i < leftValue.size(); i++) {
            result.put(Integer.toString(i), leftValue.get(i));
        }
        result.putAll(rightValue);

        return fromValue(result);
    }

    private static MetaExpression processObjectList(final LinkedHashMap<String, MetaExpression> leftValue, final List<MetaExpression> rightValue) {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>(leftValue);
        for (int i = 0; i < rightValue.size(); i++) {
            result.put(Integer.toString(i), rightValue.get(i));
        }

        return fromValue(result);
    }
}
