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

import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.*;
import nl.xillio.xill.api.errors.NotImplementedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * this class represents the list[0] object["keyValue"] and object.keyValue operations.
 */
public class FromList implements Processable {

    private final Processable list;
    private final Processable index;

    public FromList(final Processable list, final Processable index) {
        this.list = list;
        this.index = index;
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) throws RobotRuntimeException {

        MetaExpression listMeta = this.list.process(debugger).get();
        MetaExpression indexMeta = this.index.process(debugger).get();
        listMeta.registerReference();
        indexMeta.registerReference();

        InstructionFlow<MetaExpression> result = process(listMeta, indexMeta, debugger);

        result.get().preventDisposal();

        listMeta.releaseReference();
        indexMeta.releaseReference();

        result.get().allowDisposal();

        return result;
    }

    @SuppressWarnings({
            "unchecked"
    })
    private InstructionFlow<MetaExpression> process(MetaExpression listMeta, MetaExpression indexMeta, Debugger debugger) {

        if (indexMeta.getType() != ExpressionDataType.ATOMIC || indexMeta.isNull()) {
            return InstructionFlow.doResume(ExpressionBuilderHelper.NULL);
        }

        switch (listMeta.getType()) {
            case LIST:
                if (Double.isNaN(indexMeta.getNumberValue().doubleValue())) {
                    throw new RobotRuntimeException("The list does not contain any element called '" + indexMeta.getStringValue() + "' (a list does not have named elements).");
                }
                List<MetaExpression> list = listMeta.getValue();
                int index = indexMeta.getNumberValue().intValue();
                if (index < 0 || index >= list.size()) {
                    throw new RobotRuntimeException("Illegal value for list index: " + index);
                }
                MetaExpression listResult = list.get(index);
                return InstructionFlow.doResume(listResult);
            case OBJECT:
                MetaExpression objectResult = ((Map<String, MetaExpression>) listMeta.getValue()).get(indexMeta.getStringValue());
                if (objectResult == null) {
                    return InstructionFlow.doResume(ExpressionBuilderHelper.NULL);
                }
                return InstructionFlow.doResume(objectResult);
            case ATOMIC:
                throw new RobotRuntimeException("Cannot get member of ATOMIC value.");
            default:
                throw new NotImplementedException("This type has not been implemented.");
        }
    }

    @Override
    public Collection<Processable> getChildren() {
        return Arrays.asList(list, index);
    }

}
