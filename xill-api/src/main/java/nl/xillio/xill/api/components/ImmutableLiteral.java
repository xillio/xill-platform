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

import nl.xillio.xill.api.data.MetadataExpression;
import nl.xillio.xill.api.io.IOStream;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class represents a constant literal in the Xill language.
 */
class ImmutableLiteral extends MetaExpression {

    private final Expression value;

    /**
     * @param value the expression to create a constant of.
     */
    public ImmutableLiteral(final Expression value) {
        this.value = value;
        setValue(value);
    }

    @Override
    public Number getNumberValue() {
        return value.getNumberValue();
    }

    @Override
    public String getStringValue() {
        return value.getStringValue();
    }

    @Override
    public boolean getBooleanValue() {
        return value.getBooleanValue();
    }

    @Override
    public boolean isNull() {
        return value.isNull();
    }

    @Override
    public IOStream getBinaryValue() {
        return value.getBinaryValue();
    }

    @Override
    public Collection<Processable> getChildren() {
        return new ArrayList<>();
    }

    @Override
    public void close() {
        // Stub to prevent closing of literals

        // Clear out the meta pool
        closeMetaPool();

        // Make sure the reference count doesn't drop below 0
        resetReferences();
    }

    @Override
    public void storeMeta(MetadataExpression object) {
        // No OP
    }
}
