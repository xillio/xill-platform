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
package nl.xillio.xill.api.components;

import nl.xillio.xill.api.io.IOStream;

import java.util.Objects;

/**
 * This class represents the behavior of an expression that contains binary data.
 *
 * @author Thomas Biesaart
 */
class BinaryBehavior extends AbstractBehavior {

    private final IOStream value;
    private final String reference;

    BinaryBehavior(IOStream value) {
        Objects.requireNonNull(value);
        this.value = value;

        String description = value.getDescription();
        if (description == null) {
            reference = "[Stream]";
        } else {
            reference = "[Stream: " + description + "]";
        }
    }


    @Override
    public String getStringValue() {
        return reference;
    }

    @Override
    public boolean getBooleanValue() {
        return true;
    }

    @Override
    public IOStream getBinaryValue() {
        return value;
    }

    @Override
    public Expression copy() {
        // Cannot create a copy of a stream
        return ExpressionBuilderHelper.NULL;
    }
}
