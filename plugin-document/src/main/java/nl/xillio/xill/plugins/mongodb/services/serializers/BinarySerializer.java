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
package nl.xillio.xill.plugins.mongodb.services.serializers;

import me.biesaart.utils.IOUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.MetaExpressionDeserializer;
import nl.xillio.xill.api.components.MetaExpressionSerializer;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.io.SimpleIOStream;
import org.bson.types.Binary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;

public class BinarySerializer implements MetaExpressionSerializer, MetaExpressionDeserializer {

    @Override
    public MetaExpression parseObject(Object object) {
        if (!(object instanceof Binary)) {
            return null;
        }
        Binary binary = (Binary) object;

        InputStream stream = new ByteArrayInputStream(binary.getData());
        return fromValue(new SimpleIOStream(stream, "Mongo binary stream"));
    }

    @Override
    public Binary extractValue(MetaExpression metaExpression) {
        if (metaExpression.getBinaryValue().hasInputStream()) {
            try (InputStream stream = metaExpression.getBinaryValue().getInputStream()) {
                return new Binary(IOUtils.toByteArray(stream));
            } catch (IOException e) {
                throw new OperationFailedException("create stream from input", e.getMessage(), e);
            }
        }

        return null;
    }
}
