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
package nl.xillio.xill.plugins.excel.constructs;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.excel.datastructures.DateImpl;

import java.util.regex.Pattern;

/**
 * This class provides utility methods for excel constructs.
 *
 * @author Xavier Pardonnet
 */
public abstract class AbstractExcelConstruct extends Construct {

    protected static final String PARAMETER_NAME_SHEET = "sheet";
    protected static final String PARAMETER_NAME_COLUMN = "column";
    protected static final String PARAMETER_NAME_ROW = "row";

    /**
     * Verify that the meta expression is in numeric format and returns the corresponding int.
     * Used to check the validity of row or column MetaExpressions.
     *
     * @param expression    the row or column reference expression to check
     * @param parameterName the name of the parameter, to return in the error message
     * @return the value encapsulated in the parameter
     * @throws RobotRuntimeException if the parameter is not in numeric notation
     */
    protected static int assertNumeric(MetaExpression expression, String parameterName) {
        if (Double.isNaN(expression.getNumberValue().doubleValue())) {
            throw new RobotRuntimeException("Incorrect notation for " + parameterName);
        }
        return expression.getNumberValue().intValue();
    }

    /**
     * Verify that the meta expression is in alphabetical format and returns the corresponding String.
     * Used to check the validity of column MetaExpressions.
     *
     * @param expression    the column expression to check
     * @param parameterName the name of the parameter, to return in the error message
     * @return the value encapsulated in the parameter
     * @throws RobotRuntimeException if the parameter is not in alphabetical notation
     */
    protected static String assertAlpha(MetaExpression expression, String parameterName) {
        if (!Pattern.matches("[a-zA-Z]*|[0-9]*", expression.getStringValue())) {
            throw new RobotRuntimeException("Incorrect notation for " + parameterName);
        }
        return expression.getStringValue();
    }

    /**
     * Checks whether a MetaExpression is numeric.
     *
     * @param expression the MetaExpression to check
     * @return whether the input is numeric
     */
    protected static boolean isNumeric(MetaExpression expression) {
        return !Double.isNaN(expression.getNumberValue().doubleValue());
    }

    /**
     * Converts the value of an Excel cell to a usable MetaExpression.
     * Mainly ensures that dates are stored as metadata.
     *
     * @param cellValue the value of a cell
     * @return the metaExpression representing the value.
     */
    protected static MetaExpression fromCellValue(Object cellValue) {
        if (cellValue instanceof DateImpl) {
            DateImpl date = (DateImpl) cellValue;
            MetaExpression toReturn = fromValue(date.getZoned().toString());
            toReturn.storeMeta(date);
            return toReturn;
        } else {
            return parseObject(cellValue);
        }
    }

}
