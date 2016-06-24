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

import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * This enum represents the possible types of fields in a valid Decorator definition.
 * It can also convert a
 *
 * @author Thomas Biesaart
 */
public enum FieldType {
    BOOLEAN {
        @Override
        public Object extractValue(final MetaExpression e) {
            assertAtomic(e);
            return e.getBooleanValue();
        }
    },

    STRING {
        @Override
        public Object extractValue(final MetaExpression e) {
            assertAtomic(e);
            return e.getStringValue();
        }
    },

    NUMBER {
        @Override
        public Object extractValue(final MetaExpression e) {
            assertAtomic(e);
            return e.getNumberValue();
        }
    },

    DATE {
        @Override
        public Object extractValue(final MetaExpression e) {
            assertAtomic(e);
            Date metaDate = e.getMeta(Date.class);
            if (metaDate == null) {
                throw new RobotRuntimeException("Expected a date but found  " + e);
            }

            return java.util.Date.from(metaDate.getZoned().toInstant());
        }
    },

    LIST {
        @Override
        public Object extractValue(final MetaExpression e) {
            if (e.getType() != ExpressionDataType.LIST) {
                throw new RobotRuntimeException("This list field is malformed: evaluates to NULL or to different type than LIST");
            }

            return MetaExpression.extractValue(e);
        }
    };

    public static final String AVAILABLE_TYPES = StringUtils.join(values(), ", ");

    protected void assertAtomic(MetaExpression expression) {
        if (expression.getType() != ExpressionDataType.ATOMIC) {
            throw new RobotRuntimeException("A field with type " + name() + " must be an ATOMIC. A " + expression.getType() + " was passed.");
        }
    }

    public abstract Object extractValue(MetaExpression e);

    /***
     * Use this method instead of {@link #valueOf(String)}.
     * It will use a hardcoded switch instead of an array lookup.
     *
     * @param name the name of the type
     * @return the type
     */
    public static Optional<FieldType> forName(String name) {
        switch (name) {
            case "BOOLEAN":
                return Optional.of(BOOLEAN);
            case "STRING":
                return Optional.of(STRING);
            case "NUMBER":
                return Optional.of(NUMBER);
            case "DATE":
                return Optional.of(DATE);
            case "LIST":
                return Optional.of(LIST);
            default:
                return Optional.empty();
        }
    }
}
