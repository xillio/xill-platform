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

/**
 * This class represents a null implementation of the expression class.
 *
 * @author Thomas Biesaart
 */
abstract class AbstractBehavior implements Expression {
    private IOStream emptyStream;

    @Override
    public Number getNumberValue() {
        return Double.NaN;
    }

    @Override
    public String getStringValue() {
        return "";
    }

    @Override
    public boolean getBooleanValue() {
        return false;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public IOStream getBinaryValue() {
        if (emptyStream == null) {
            emptyStream = new EmptyIOStream();
        }
        return emptyStream;
    }

    @Override
    public String toString() {
        return getStringValue();
    }

    @Override
    public void close() {
        getBinaryValue().close();
    }
}
